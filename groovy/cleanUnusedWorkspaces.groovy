import hudson.node_monitors.*;
 
// Doesn't support custom workspaces

def processFolder(node, folder) {
  
  def folderWs = node.getWorkspaceFor(folder)
  
  if (!folderWs.exists()) {
    return
  }
  
  def folderDirs = folderWs.list().collect { it.getName() }
  
  folder.items.each { item ->
    processItem(node, item)
    
    def dirName = item.getName()
    
    folderDirs.remove(dirName)
    folderDirs.remove(dirName + "@tmp")
    folderDirs.remove(dirName + "@libs")
    folderDirs.remove(dirName + "@script")
  }
  
  folderDirs.each {
  	def unusedWs = new FilePath(folderWs, it)
    if (unusedWs.exists()) {
      println "Delete unused workspace ${unusedWs}"
      unusedWs.deleteRecursive()
    }
  }
}

def processItem(node, item) {
  if (item instanceof com.cloudbees.hudson.plugins.folder.Folder) {
  	processFolder(node, (com.cloudbees.hudson.plugins.folder.Folder)item)
  }	
}

Jenkins.instance.nodes.each { node ->
  
  computer = node.toComputer()
  if (computer.getChannel() == null) return
    
  diskSize = DiskSpaceMonitor.DESCRIPTOR.get(computer).size
  roundedSize = diskSize / (1024 * 1024 * 1024) as int
  
  println("node: " + node.getDisplayName() + ", rootPath: " + node.getRootPath() + ", workspace: " + node.getWorkspaceRoot() + ", free space: " + roundedSize + "GB")
  
  Jenkins.instance.getAllItems(AbstractItem.class).each { item ->
    processItem(node, item)
  }
}
 
return null
