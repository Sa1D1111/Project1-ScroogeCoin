public class TxHandler {

    /* Creates a public ledger whose current UTXOPool (collection of unspent 
     * transaction outputs) is utxoPool. This should make a defensive copy of 
     * utxoPool by using the UTXOPool(UTXOPool uPool) constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
    }

    /* Returns true if 
     * (1) all outputs claimed by tx are in the current UTXO pool, 
     * (2) the signatures on each input of tx are valid, 
     * (3) no UTXO is claimed multiple times by tx, 
     * (4) all of tx’s output values are non-negative, and
     * (5) the sum of tx’s input values is greater than or equal to the sum of   
            its output values;
       and false otherwise.
     */

    public boolean isValidTx(Transaction tx) {

        /* (1) ALL OUTPUTS CLAIMED BY TX ARE IN THE CURRENT UTXO POOL */
        
        /* (2) SIGNATURES ON EACH INPUT OF TX ARE VALID */
        //for (int i = 0 ; i < tx.numInputs() ; i++) { if (!verifySignature(tx.getRawDataToSign(i), tx.getInput(i).signature)) { return false ; } } ;

        /* (3) NO UTXO IS CLAIMED MULTIPLE TIMES BY TX */

        /* (4) ALL OF TX'S OUTPUT VALUES ARE NON-NEGATIVE */
        for (Transaction.Output output : tx.getOutputs()) { if (output.value < 0) { return false ; } }

        /* (5) SUM OF TX'S INPUT VALUES IS GREATER THAN OR EQUAL TO THE SUM OF ITS OUTPUT VALUES */
        double inputSum = 0 ;
        for (Transaction.Input input : tx.getInputs()) { /* inputSum += input. USE THE OUTPUT VALUE FROM THE PREVIOUS TRANSACTION BEING USED */; }
        
        double outputSum = 0 ;
        for (Transaction.Output output : tx.getOutputs()) { outputSum += output.value ; }

        if (inputSum >= outputSum) { return true ; }

        return true ;
    }

    /* Handles each epoch by receiving an unordered array of proposed 
     * transactions, checking each transaction for correctness, 
     * returning a mutually valid array of accepted transactions, 
     * and updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
        return null; // RETURN A MUTUALLY VALID TRANSACTION SET OF MAXIMAL SIZE
    }

} 
