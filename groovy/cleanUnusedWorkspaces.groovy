import hudson.node_monitors.*;
 
// Doesn't support custom workspaces

def cleanNode(node) {
  computer = node.toComputer()
  if (computer.getChannel() == null) return

  rootPath = node.getRootPath()
  wsPath = new FilePath(rootPath, 'workspace')
  size = DiskSpaceMonitor.DESCRIPTOR.get(computer).size
  roundedSize = size / (1024 * 1024 * 1024) as int
  println("node: " + node.getDisplayName() + ", rootPath: " + rootPath + ", workspace: " + wsPath + ", free space: " + roundedSize + "GB")
 
  nodeWss = Jenkins.instance.items.collect { node.getWorkspaceFor(it) }.collect { it.getName() }
 
  wsNames = wsPath.list().collect { it.getName() }.findAll { !it.endsWith("@tmp") && !it.endsWith("@libs") && !it.endsWith("@script") }
  wsNames.removeAll(nodeWss)
  wsNames.each { name ->
    [new FilePath(wsPath, name), new FilePath(wsPath, name + "@tmp")].each { unusedWs ->
      if (unusedWs.exists()) {
        println "Remove unused ws: " + unusedWs
        unusedWs.deleteRecursive()
      }
    }
  }
}

cleanNode(Jenkins.get()) 
Jenkins.get().nodes.each { cleanNode(it); }
 
return null
