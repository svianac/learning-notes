import jenkins.model.Jenkins
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval

// Ensure Jenkins instance is available
def jenkinsInstance = Jenkins.getInstanceOrNull()
if (jenkinsInstance == null) {
    println("Jenkins instance is not available.")
    return
}

// Get the ScriptApproval instance
def scriptApproval = ScriptApproval.get()

if (scriptApproval == null) {
    println("ScriptApproval instance is not available.")
} else {
    // Approve all pending signatures
    def pendingSignatures = scriptApproval.getPendingSignatures()
    
    if (pendingSignatures.isEmpty()) {
        println("No pending signatures to approve.")
    } else {
        pendingSignatures.each { signature ->
            println("Approving signature: ${signature}")
            scriptApproval.approveSignature(signature)
        }
        println("All pending signatures approved.")
    }
}