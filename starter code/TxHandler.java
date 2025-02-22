import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;

public class TxHandler {
    /* Creates a public ledger whose current UTXOPool (collection of unspent 
     * transaction outputs) is utxoPool. This should make a defensive copy of 
     * utxoPool by using the UTXOPool(UTXOPool uPool) constructor.
     */
    private UTXOPool utxoPool; // pool of unspent transactions

    public TxHandler(UTXOPool utxoPool) {
        this.utxoPool = new UTXOPool(utxoPool); // initializes transaction handler with copy of given UTXO pool
    }

    /**********************************************************************************
     * public boolean isValidTx(Transaction tx)
     *      Returns true if 
     *      (1) all outputs claimed by tx are in the current UTXO pool, 
     *      (2) the signatures on each input of tx are valid, 
     *      (3) no UTXO is claimed multiple times by tx, 
     *      (4) all of tx’s output values are non-negative, and
     *      (5) the sum of tx’s input values is greater than or equal to the sum of   
                its output values;
            and false otherwise.
     **********************************************************************************/
    public boolean isValidTx(Transaction tx) 
    {
        Set<UTXO> usedUTXOs = new HashSet<>(); // store used UTXOs to detect double spending
        ArrayList<Transaction.Input> inputs = tx.getInputs(); 
        ArrayList<Transaction.Output> outputs = tx.getOutputs();
        double inputSum = 0;
        double outputSum = 0;

        for (int i = 0; i < tx.numInputs(); i++) {
            Transaction.Input input = inputs.get(i); 
            UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex); // UTXO based on prev transaction hash and output index
            Transaction.Output output = utxoPool.getTxOutput(utxo); // retrieve corresponding output from pool

        /* (1) ALL OUTPUTS CLAIMED BY TX ARE IN THE CURRENT UTXO POOL */
        if (!utxoPool.contains(utxo)) { return false; }

        /* (2) SIGNATURES ON EACH INPUT OF TX ARE VALID */
        RSAKey rsa = output.address; 
        if (!rsa.verifySignature(tx.getRawDataToSign(i), tx.getInput(i).signature)) { return false; } // attempt to verify signature

        /* (3) NO UTXO IS CLAIMED MULTIPLE TIMES BY TX */
        if (!usedUTXOs.add(utxo)) { return false; } // 

        inputSum += output.value; // add input value to sum variable
        }

        for (Transaction.Output output : outputs) { 
        /* (4) ALL OF TX'S OUTPUT VALUES ARE NON-NEGATIVE */
            if (output.value < 0) { return false; } 
            
        /* (5) SUM OF TX'S INPUT VALUES IS GREATER THAN OR EQUAL TO THE SUM OF ITS OUTPUT VALUES */
            outputSum += output.value; 
        }

        if (!(inputSum >= outputSum)) { return false; }

        return true;
    }

    /* Handles each epoch by receiving an unordered array of proposed 
     * transactions, checking each transaction for correctness, 
     * returning a mutually valid array of accepted transactions, 
     * and updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) 
    {
        if (possibleTxs == null) { return new Transaction[0] ; } // Return an empty array if possibleTxs is null

        ArrayList<Transaction> acceptedTxs = new ArrayList<>();
        
        for (Transaction tx : possibleTxs) 
        {
            if (tx == null) { continue ; } // Skip null transactions if any exist in the array
            if (tx.getInputs() == null || tx.getOutputs() == null) { continue ; } // Skip transactions with null inputs or outputs

            if (isValidTx(tx)) 
            {
                acceptedTxs.add(tx) ; // add the accepted transaction to the list
                
                /* REMOVE CONSUMED COIN FROM POOL */
                ArrayList<Transaction.Input> inputs = tx.getInputs() ; 
                for (Transaction.Input input : inputs) 
                { 
                    UTXO spentUTXO = new UTXO(input.prevTxHash, input.outputIndex) ; // create UTXO from input
                    utxoPool.removeUTXO(spentUTXO) ; // remove spent UTXO from pool
                }

                /* ADD GENERATED COIN TO POOL */
                ArrayList<Transaction.Output> outputs = tx.getOutputs() ; 
                for (int i = 0 ; i < tx.numOutputs() ; i++)
                {
                    Transaction.Output output = outputs.get(i) ; 
                    UTXO newUTXO = new UTXO(tx.getHash(), i) ; // create new UTXO using transaction hash and output index
                    utxoPool.addUTXO(newUTXO, output) ; // add new UTXO to pool
                }
            }
        }
        /* CONVERT LIST OF VALID TRANSACTIONS TO ARRAY AND RETURN */
        return acceptedTxs.toArray(new Transaction[0]); // Return accepted transactions as array
    }
}