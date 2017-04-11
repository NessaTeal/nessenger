package org.nenl.chatstorage;

import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

public class DatabaseCleanerDaemon implements Runnable {

	private static Logger logger = LoggerFactory.getLogger(DatabaseCleanerDaemon.class);
	
	protected MongoCollection<Document> messageCollection;
	
	public DatabaseCleanerDaemon(MongoCollection<Document> messageCollection) {
		this.messageCollection = messageCollection;
	}
	
	@Override
	public void run() {
		
		while(true) {
		
			long timeLimit = System.currentTimeMillis() - 24 * 60 * 60 * 1000;
			
			messageCollection.deleteMany(Filters.lt("date", timeLimit)).toString();
			
			try {
				TimeUnit.HOURS.sleep(1);
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
			}
		}
	}

}
