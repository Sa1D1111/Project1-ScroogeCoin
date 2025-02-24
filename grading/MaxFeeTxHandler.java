import java.util.HashSet ;
import java.util.Set ;
import java.util.ArrayList ;
import java.util.Collections;
import java.util.Comparator;

public class MaxFeeTxHandler {

    private UTXOPool utxoPool;

    public MaxFeeTxHandler(UTXOPool utxoPool) {
        this.utxoPool = new UTXOPool(utxoPool); // Make a defensive copy of the UTXO pool
    }

    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        if (possibleTxs == null) {
            return new Transaction[0];
        }

        // List to store valid transactions
        ArrayList<Transaction> acceptedTxs = new ArrayList<>();

        // List to store transactions with their corresponding fees
        ArrayList<FeeTransaction> feeTxs = new ArrayList<>();

        // Calculate the fee for each transaction
        for (Transaction tx : possibleTxs) {
            if (tx == null || tx.getInputs() == null || tx.getOutputs() == null) {
                continue;
            }

            if (isValidTx(tx)) {
                double inputSum = 0;
                double outputSum = 0;

                // Calculate input sum
                for (Transaction.Input input : tx.getInputs()) {
                    UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
                    Transaction.Output output = utxoPool.getTxOutput(utxo);
                    inputSum += output.value;
                }

                // Calculate output sum
                for (Transaction.Output output : tx.getOutputs()) {
                    outputSum += output.value;
                }

                // Calculate fee
                double fee = inputSum - outputSum;
                feeTxs.add(new FeeTransaction(tx, fee));
            }
        }

        // Sort transactions by fee in descending order (largest fee first)
        Collections.sort(feeTxs, new Comparator<FeeTransaction>() {
            public int compare(FeeTransaction t1, FeeTransaction t2) {
                return Double.compare(t2.fee, t1.fee); // descending order
            }
        });

        // Track UTXOs that have been used
        Set<UTXO> usedUTXOs = new HashSet<>();

        // Process transactions by selecting those with the highest fees
        for (FeeTransaction feeTx : feeTxs) {
            Transaction tx = feeTx.tx;

            // Check if transaction is valid
            if (isValidTx(tx)) {
                boolean isValid = true;

                // Check if any UTXO in this transaction is already used (double-spending)
                for (Transaction.Input input : tx.getInputs()) {
                    UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
                    if (usedUTXOs.contains(utxo)) {
                        isValid = false;
                        break;
                    }
                }

                if (isValid) {
                    // Accept the transaction and update the UTXO pool
                    acceptedTxs.add(tx);

                    // Mark UTXOs as used and update the UTXO pool
                    for (Transaction.Input input : tx.getInputs()) {
                        UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
                        usedUTXOs.add(utxo);
                        utxoPool.removeUTXO(utxo); // Remove spent UTXOs
                    }

                    // Add new UTXOs from outputs
                    for (int i = 0; i < tx.numOutputs(); i++) {
                        Transaction.Output output = tx.getOutputs().get(i);
                        UTXO newUTXO = new UTXO(tx.getHash(), i);
                        utxoPool.addUTXO(newUTXO, output); // Add new UTXOs to pool
                    }
                }
            }
        }

        return acceptedTxs.toArray(new Transaction[0]);
    }

    private boolean isValidTx(Transaction tx) {
        Set<UTXO> usedUTXOs = new HashSet<>();
        ArrayList<Transaction.Input> inputs = tx.getInputs();
        ArrayList<Transaction.Output> outputs = tx.getOutputs();
        double inputSum = 0;
        double outputSum = 0;

        for (int i = 0; i < tx.numInputs(); i++) {
            Transaction.Input input = inputs.get(i);
            UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
            Transaction.Output output = utxoPool.getTxOutput(utxo);

            // Check if output is in UTXO pool
            if (!utxoPool.contains(utxo)) {
                return false;
            }

            // Verify the signature
            RSAKey rsa = output.address;
            if (!rsa.verifySignature(tx.getRawDataToSign(i), tx.getInput(i).signature)) {
                return false;
            }

            // Check for double-spending
            if (!usedUTXOs.add(utxo)) {
                return false;
            }

            inputSum += output.value;
        }

        for (Transaction.Output output : outputs) {
            if (output.value < 0) {
                return false;
            }
            outputSum += output.value;
        }

        return inputSum >= outputSum;
    }

    // Helper class to store transactions with their fee
    private class FeeTransaction {

        Transaction tx;
        double fee;

        FeeTransaction(Transaction tx, double fee) {
            this.tx = tx;
            this.fee = fee;
        }
    }
}
