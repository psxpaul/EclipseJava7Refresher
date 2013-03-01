package com.pr.jdkseven;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import org.eclipse.core.runtime.Plugin;

public class Java7RefreshProviderPlugin extends Plugin {

	public static void main(String[] args) throws Exception {
		Path path = Paths.get("/home/proberts/Desktop");
		WatchService watchService = path.getFileSystem().newWatchService();
		
		path.register(watchService,
				StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE);
		
		while(true) {
			WatchKey watchKey = watchService.take();
			
			for (WatchEvent<?> event : watchKey.pollEvents()) {
                printEvent(event);
            }
			
			if(!watchKey.reset()) {
				watchKey.cancel();
				watchService.close();
			}
		}
	}

	private static void printEvent(WatchEvent<?> event) {
		Path pathCreated = (Path) event.context();
        System.out.println("Changed: " + pathCreated);		
	}
}
