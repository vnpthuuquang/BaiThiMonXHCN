import java.util.ArrayList;
import java.util.Date;

public class VNPT_Quang {
    public String hash;
    public String previousHash;
    public String merkleRoot;
    public ArrayList<Transaction> transactions = new ArrayList<Transaction>();
    public long timeStamp;
    public int nonce;
    public VNPT_Quang(String previousHash){
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.hash = calculateHash();
    }
    public String calculateHash(){
        String calculatedhash = StringUtil.applySha256(
                previousHash + Long.toString(timeStamp) +
                        Integer.toString(nonce) + merkleRoot
        );
        return calculatedhash;
    }
    public void mineBlock(int difficulty){
        merkleRoot = StringUtil.getMerkleRoot(transactions);
        String target = StringUtil.getDificultyString(difficulty);
        while(!hash.substring( 0, difficulty).equals(target)){
            nonce ++;
            hash = calculateHash();
        }
        System.out.println("Block Mined!!! : " + hash);
    }
    public boolean addTransaction(Transaction transaction){
        if(transaction == null) return false;
        if((!"0".equals(previousHash))){
            if((transaction.processTransaction() != true)){
                System.out.println("Giao dịch không xử lý được!.");
                return false;
            }
        }
        transactions.add(transaction);
        System.out.println("Giao dịch đã được thêm thành công vào khối.");
        return true;
    }
}
