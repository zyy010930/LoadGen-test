package scs.util.loadGen.driver.sdcbench;
   
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import scs.util.loadGen.driver.AbstractJobDriver;
import scs.util.loadGen.threads.LoadExecThread;
import scs.util.repository.Repository;
import scs.util.tools.HttpClientPool; 
/**
 * Image recognition service request class
 * GPU inference
 * @author Yanan Yang
 *
 */
public class SdcbenchResNet50Driver extends AbstractJobDriver{
	/**
	 * Singleton code block
	 */
	private static SdcbenchResNet50Driver driver=null;	
	public SdcbenchResNet50Driver(){initVariables();}
	public synchronized static SdcbenchResNet50Driver getInstance() {
		if (driver == null) {
			driver = new SdcbenchResNet50Driver();
		}
		return driver;
	}
 
	@Override
	protected void initVariables() {
		httpClient=HttpClientPool.getInstance().getConnection();
		queryItemsStr=Repository.resNet50BaseURL; 
		jsonParmStr=Repository.resNet50ParmStr; 
		queryItemsStr=queryItemsStr.replace("Ip", "192.168.1.106"); 
		queryItemsStr=queryItemsStr.replace("Port", "30100"); 
	}

	/**
	 * using countDown to send requests in open-loop
	 */
	public void executeJob(int serviceId) {  
		ExecutorService executor = Executors.newCachedThreadPool();
	 
		Repository.onlineQueryThreadRunning[serviceId]=true;
		Repository.sendFlag[serviceId]=true;
		while(Repository.onlineDataFlag[serviceId]==true){
			if(Repository.sendFlag[serviceId]==true&&Repository.realRequestIntensity[serviceId]>0){
				CountDownLatch begin=new CountDownLatch(1);
				int sleepUnit=1000/Repository.realRequestIntensity[serviceId];
				for (int i=0;i<Repository.realRequestIntensity[serviceId];i++){ 
					executor.execute(new LoadExecThread(httpClient,queryItemsStr,begin,serviceId,jsonParmStr,sleepUnit*i,"POST"));
				}
				Repository.sendFlag[serviceId]=false;
				Repository.totalRequestCount[serviceId]+=Repository.realRequestIntensity[serviceId];
				begin.countDown();
			}else{
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				//System.out.println("loader watting "+TestRepository.list.size());
			}
		}
		executor.shutdown();
		while(!executor.isTerminated()){
			try {
				Thread.sleep(2000);
			} catch(InterruptedException e){
				e.printStackTrace();
			}
		}  
		Repository.onlineQueryThreadRunning[serviceId]=false; 
	}
}