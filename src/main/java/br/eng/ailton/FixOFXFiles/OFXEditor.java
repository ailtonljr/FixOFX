package br.eng.ailton.FixOFXFiles;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.webcohesion.ofx4j.domain.data.MessageSetType;
import com.webcohesion.ofx4j.domain.data.ResponseEnvelope;
import com.webcohesion.ofx4j.domain.data.ResponseMessageSet;
import com.webcohesion.ofx4j.domain.data.banking.BankStatementResponseTransaction;
import com.webcohesion.ofx4j.domain.data.banking.BankingResponseMessageSet;
import com.webcohesion.ofx4j.domain.data.common.Transaction;
import com.webcohesion.ofx4j.domain.data.creditcard.CreditCardResponseMessageSet;
import com.webcohesion.ofx4j.domain.data.creditcard.CreditCardStatementResponseTransaction;
import com.webcohesion.ofx4j.io.AggregateMarshaller;
import com.webcohesion.ofx4j.io.AggregateUnmarshaller;
import com.webcohesion.ofx4j.io.OFXParseException;
import com.webcohesion.ofx4j.io.v1.OFXV1Writer;


/**
 * @author Ailton Luiz Dias Siqueira Junior
 *
 */
public class OFXEditor {
	
	
	
	private File ofxFile;

	public File getOfxFile() {
		return ofxFile;
	}

	public void setOfxFile(File ofxFile) {
		this.ofxFile = ofxFile;
	}
	
	private ResponseEnvelope responseEnvelope;
	
	private ResponseMessageSet loadOfxFile(MessageSetType type) throws FileNotFoundException, IOException
	{
		
		AggregateUnmarshaller<ResponseEnvelope> unmarshaller = new AggregateUnmarshaller<ResponseEnvelope>(
				ResponseEnvelope.class);

		
		
		ResponseMessageSet responseMessageSet = null;
		
		try {
			responseEnvelope = unmarshaller.unmarshal(new FileInputStream(ofxFile));
			
			//SignonResponse sr = responseEnvelope.getSignonResponse();
			
			responseMessageSet = responseEnvelope.getMessageSet(type);
			
		} catch (OFXParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		return responseMessageSet;
	}
	
	
	private void saveOfxFile(ResponseMessageSet messageSet) throws IOException {
		
		//StringWriter marshalled = new StringWriter();
		File fileToSave = new File(ofxFile.getParent() + File.separator + "Fixed" + ofxFile.getName());
		
		FileWriter marshalled = new FileWriter(fileToSave);
		
		OFXV1Writer writer = new OFXV1Writer(marshalled);
		
		AggregateMarshaller marsheller = new AggregateMarshaller();
		marsheller.marshal(responseEnvelope, writer);
		
		writer.close();
		
		
		
		

		/*try (PrintWriter out = new PrintWriter(fileToSave.getAbsolutePath())) {
			out.println(marshalled.toString());
			
		}*/
		
	}
	
	public void FixBalanceOnCaixa(double creditLimit) {
		
		
		try {
			
			ResponseMessageSet message = loadOfxFile(MessageSetType.banking);
			
			
			//Find balace and subtract the credit limit
			if (message != null) {
				List<BankStatementResponseTransaction> bank = ((BankingResponseMessageSet) message)
						.getStatementResponses();
				for (BankStatementResponseTransaction b : bank) {
					System.out.println("cc: " + b.getMessage().getAccount().getAccountNumber());
					// System.out.println("ag: " + b.getMessage().getAccount().getBranchId());
					double balance = b.getMessage().getLedgerBalance().getAmount();
					System.out.println("balanço final: " + b.getMessage().getLedgerBalance().getAmount());
					
					
					
					double newBalance = balance - creditLimit;
					BigDecimal modelVal = new BigDecimal(newBalance);
					BigDecimal displayVal = modelVal.setScale(2, RoundingMode.HALF_EVEN);
					newBalance = displayVal.doubleValue();
					b.getMessage().getLedgerBalance().setAmount(newBalance);
					
					System.out.println("balanço final Corrigido: " + b.getMessage().getLedgerBalance().getAmount());
					
					
					
					System.out.println("dataDoArquivo: " + b.getMessage().getLedgerBalance().getAsOfDate());
					List<Transaction> list = b.getMessage().getTransactionList().getTransactions();
					System.out.println("TRANSAÇÕES\n");
					for (Transaction transaction : list) {
						System.out.println("tipo: " + transaction.getTransactionType().name());
						System.out.println("id: " + transaction.getId());
						System.out.println("data: " + transaction.getDatePosted());
						System.out.println("valor: " + transaction.getAmount());
						System.out.println("descricao: " + transaction.getMemo());
						System.out.println("");
					}
				}
			}
			
			
			
			saveOfxFile(message);
			
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	
	private final Pattern visaInstallmentPattern = Pattern.compile("PARC \\d+\\/\\d+");
	
	public void FixVisa() {
		
		
		try {
			
			ResponseMessageSet message = loadOfxFile(MessageSetType.creditcard);
			
			
			//Find balace and subtract the credit limit
			if (message != null) {
				List<CreditCardStatementResponseTransaction> bank = ((CreditCardResponseMessageSet) message)
						.getStatementResponses();
				for (CreditCardStatementResponseTransaction b : bank) {
					System.out.println("cc: " + b.getMessage().getAccount().getAccountNumber());
					// System.out.println("ag: " + b.getMessage().getAccount().getBranchId());
			
					System.out.println("balanço final: " + b.getMessage().getLedgerBalance().getAmount());

					System.out.println("dataDoArquivo: " + b.getMessage().getLedgerBalance().getAsOfDate());
					List<Transaction> list = b.getMessage().getTransactionList().getTransactions();
					System.out.println("TRANSAÇÕES\n");
					for (Transaction transaction : list) {
						System.out.println("tipo: " + transaction.getTransactionType().name());
						System.out.println("id: " + transaction.getId());
						System.out.println("data: " + transaction.getDatePosted());
						
						Matcher memoMatcher = visaInstallmentPattern.matcher(transaction.getMemo());
						
						
						
						if(memoMatcher.find() && 
								!transaction.getMemo().startsWith("ANUIDADE") && 
								!transaction.getMemo().startsWith("DESCONTO ADC"))
						{
							
							
							int installmentNumber = Integer.parseInt( memoMatcher.group().substring(5, 7) );
							
							Date date = transaction.getDatePosted();
							
							Calendar cal = Calendar.getInstance();
							cal.setTime(date); // your date (java.util.Date)
							cal.add(Calendar.MONTH, installmentNumber - 1); // You can -/+ x months here to go back in history or move forward.
							date = cal.getTime(); // New date
							transaction.setDatePosted(date);
							System.out.println("data modificada: " + transaction.getDatePosted());
							
						}
						
						
						System.out.println("valor: " + transaction.getAmount());
						System.out.println("descricao: " + transaction.getMemo());
						System.out.println("");
					}
				}
			}
			
			
			
			saveOfxFile(message);
			
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	

}
