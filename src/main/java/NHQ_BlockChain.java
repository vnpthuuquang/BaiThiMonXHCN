import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class NHQ_BlockChain {
    public static ArrayList<VNPT_Quang> blockchain = new ArrayList<>();
    public static HashMap<String,TransactionOutput> UTXOs = new HashMap<>();
    public static int difficulty = 5;
    public static float minimumTransaction = 0.1f;
    public static Store store1;
    public static Store store2;
    public static Transaction genesisTransaction;

    public static Transaction genesisTransaction2;

    public static void main(String[] args){
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        store1 = new Store();
        store2 = new Store();
        Store mobilebase = new Store();

        Scanner BlockData = new Scanner(System.in);
        System.out.println("Nhập số lượng điện thoại kho 1: ");
        int mobile1 = Integer.parseInt(BlockData.nextLine());
        System.out.println("Nhập số lượng điện thoại kho 2: ");
        int mobile2 = Integer.parseInt(BlockData.nextLine());
        System.out.println("Nhập số lượng điện thoại kho 1 chuyển sang kho 2: ");
        int mobile_move = Integer.parseInt(BlockData.nextLine());

        genesisTransaction = new Transaction(mobilebase.publicKey, store1.publicKey, mobile1, null);
        genesisTransaction.generateSignature(mobilebase.privateKey);
        genesisTransaction.transactionId = "0";
        genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.reciepient, genesisTransaction.value, genesisTransaction.transactionId));
        UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

        genesisTransaction2 = new Transaction(mobilebase.publicKey, store2.publicKey, mobile2, null);
        genesisTransaction2.generateSignature(mobilebase.privateKey);
        genesisTransaction2.transactionId = "1";
        genesisTransaction2.outputs.add(new TransactionOutput(genesisTransaction2.reciepient, genesisTransaction2.value, genesisTransaction2.transactionId));
        UTXOs.put(genesisTransaction2.outputs.get(0).id, genesisTransaction2.outputs.get(0));

        System.out.println("Đang tạo và đào khối gốc .... ");
        VNPT_Quang genesis = new VNPT_Quang("0");
        genesis.addTransaction(genesisTransaction);
        genesis.addTransaction(genesisTransaction2);
        addBlock(genesis);
        VNPT_Quang[] mobileObject = new VNPT_Quang[100];
        mobileObject[0] = new VNPT_Quang(genesis.hash);
        mobileObject[0].addTransaction(store1.sendFunds(store2.publicKey, mobile_move));
        addBlock(mobileObject[0]);
        int index = 0;
        while (mobile_move > mobile1){
            System.out.println("\nSố lượng điện thoại chuyển nhiều hơn số điện thoại có trong kho 1");
            System.out.println("\n Hãy nhập lại số lượng điện thoại kho 1 chuyển sang kho 2: ");
            mobile_move = Integer.parseInt(BlockData.nextLine());
            mobileObject[index+1] = new VNPT_Quang(mobileObject[index].hash);
            mobileObject[index+1].addTransaction(store1.sendFunds(store2.publicKey, mobile_move));
            addBlock(mobileObject[index+1]);
            index++;

        }
        System.out.println("\nSố lượng điện thoại mới trong kho 1 là : " + store1.getBalance());
        System.out.println("\nSố lượng điện thoại mới trong kho 2 là : " + store2.getBalance());

        isChainValid();
    }
    public static boolean isChainValid(){
        VNPT_Quang currentBlock;
        VNPT_Quang previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0','0');
        HashMap<String,TransactionOutput> tempUTXOs = new HashMap<String,TransactionOutput>();
        tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));
        for (int i=1; i < blockchain.size(); i++){
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i-1);
            if (!currentBlock.hash.equals(currentBlock.calculateHash())){
                System.out.println("#Mã băm khối hiện tại không khớp");
                return false;
            }
            if (!previousBlock.hash.equals(currentBlock.previousHash)){
                System.out.println("#Mã băm khối trước không khớp");
                return false;
            }
            if (!currentBlock.hash.substring( 0, difficulty).equals(hashTarget)){
                System.out.println("#Khối này không đào được do lỗi!");
                return false;
            }
            TransactionOutput tempOutput;

            for(int t=0; t <currentBlock.transactions.size(); t++) {
                Transaction currentTransaction = currentBlock.transactions.get(0);

                if(!currentTransaction.verifySignature()) {
                    System.out.println("#Chữ ký số của giao dịch (" + t + ") không hợp lệ");
                    return false;
                }
                if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
                    System.out.println("#Các đầu vào không khớp với đầu ra trong giao dịch (" + t + ")");
                    return false;
                }


                for(TransactionInput input: currentTransaction.inputs) {
                    tempOutput = tempUTXOs.get(input.transactionOutputId);

                    if(tempOutput == null) {
                        System.out.println("#Các đầu vào tham chiếu trong giao dịch (" + t + ") bị thiếu!");
                        return false;
                    }

                    if(input.UTXO.value != tempOutput.value) {
                        System.out.println("#Các đầu vào tham chiếu trong giao dịch (" + t + ") có giá trị không hợp lệ");
                        return false;
                    }

                    tempUTXOs.remove(input.transactionOutputId);
                }

                for(TransactionOutput output: currentTransaction.outputs) {
                    tempUTXOs.put(output.id, output);
                }

                if( currentTransaction.outputs.get(0).reciepient != currentTransaction.reciepient) {
                    System.out.println("#Giao dịch(" + t + ") có người nhận không đúng!");
                    return false;
                }
                if( currentTransaction.outputs.get(1).reciepient != currentTransaction.sender) {
                    System.out.println("#Đầu ra của giao (" + t + ") không đúng với người gửi.");
                    return false;
                }

            }
        }
        System.out.println("Chuỗi khối hợp lệ!");
        return true;
    }
    public static void addBlock(VNPT_Quang newBlock){
        newBlock.mineBlock(difficulty);
        blockchain.add(newBlock);
    }
}
