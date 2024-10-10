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
    // Get all pending signatures
    def pendingSignatures = scriptApproval.getPendingSignatures()

    if (pendingSignatures.isEmpty()) {
        println("No pending signatures to approve.")
    } else {
        // Approve each pending signature
        pendingSignatures.each { pendingSignature ->
            println("Approving signature: ${pendingSignature.signature}")
            scriptApproval.approveSignature(pendingSignature.signature)
        }
        println("All pending signatures approved.")
    }
}