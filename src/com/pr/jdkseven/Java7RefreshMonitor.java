package com.pr.jdkseven;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.refresh.IRefreshMonitor;
import org.eclipse.core.resources.refresh.IRefreshResult;
import org.eclipse.core.runtime.IPath;

public class Java7RefreshMonitor implements IRefreshMonitor, Runnable {
	private WatchService watchService;
//	private FileWriter fw;
	private HashMap<WatchKey, PathResourceResult> keyPathMap;

	public Java7RefreshMonitor() throws Exception {
		this.watchService = FileSystems.getDefault().newWatchService();
		this.keyPathMap = new HashMap<WatchKey, PathResourceResult>();
//		this.fw = new FileWriter(File.createTempFile("j7notifier", ".log", new File("/tmp")));
		
		new Thread(this).start();
	}
	
	private class PathResourceResult {
		Path path;
		IResource resource;
		IRefreshResult result;
		
		public PathResourceResult(Path path, IResource resource, IRefreshResult result) {
			this.path = path;
			this.resource = resource;
			this.result = result;
		}
	}
	
	@Override
	public void unmonitor(IResource resource) {
		for(Entry<WatchKey, PathResourceResult> entry : keyPathMap.entrySet()) {
			if(entry.getValue() == resource) {
				keyPathMap.remove(entry.getKey());
			}
		}
	}
	
	public void monitor(IResource resource, IRefreshResult result) throws Exception {
		final IPath ipath = resource.getLocation();
		if(result == null || ipath == null) return;

		Path path = Paths.get(ipath.toFile().toURI());
		registerAll(path, resource, result);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		while(true) {
			try {
				log("waiting for watchKey...");
				WatchKey watchKey = watchService.take();
				PathResourceResult pathResourceResult = keyPathMap.get(watchKey);
				
				if(pathResourceResult == null || !watchKey.isValid()) {
					log("either watchKey resource doesn't exist, or watchKey isn't valid. Continuing...");
					continue;	//the key you just took has been removed, so wait for the next key
				}
				
				Path directory = pathResourceResult.path;
				
				List<WatchEvent<?>> pollEvents = watchKey.pollEvents();
				for(WatchEvent<?> event : pollEvents) {
	                Path filename = ((WatchEvent<Path>) event).context();
	                Path newFile = directory.resolve(filename);
	                
					if(event.kind() == StandardWatchEventKinds.ENTRY_CREATE)
						if(Files.isDirectory(newFile))
							register(newFile, pathResourceResult.resource, pathResourceResult.result);
					
					IContainer[] containers = ResourcesPlugin.getWorkspace().getRoot().findContainersForLocationURI(newFile.toUri());
					IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(newFile.toUri());
					
					for(IContainer container : containers)
						refresh(container, pathResourceResult.result);
					
					for(IFile file : files)
						refresh(file, pathResourceResult.result);
				}
				
				if(!watchKey.reset()) {
					watchKey.cancel();
				}
			} catch (Exception e) {
				log(e);
			}
		}
	}
	
	// register directory and sub-directories
    private void registerAll(final Path start, final IResource resource, final IRefreshResult result) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            	register(dir, resource, result);
                return FileVisitResult.CONTINUE;
            }
        });
    }
    
    private void register(Path path, IResource resource, IRefreshResult result) throws IOException {
    	WatchKey watchKey = path.register(watchService,
				StandardWatchEventKinds.ENTRY_CREATE,
				StandardWatchEventKinds.ENTRY_DELETE,
				StandardWatchEventKinds.ENTRY_MODIFY);
    	
    	keyPathMap.put(watchKey, new PathResourceResult(path, resource, result));
    }
    
	private void refresh(IResource resource, IRefreshResult result) {
		if (result != null && !resource.isSynchronized(IResource.DEPTH_INFINITE)) {
			result.refresh(resource);
		}
	}

	private void log(Throwable t) {
		/*try {
			t.printStackTrace(new PrintWriter(fw));
			fw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
	}
	
	private void log(String string) {
		/*try {
			fw.write(string + "\n");
			fw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
	}
}