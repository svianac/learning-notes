import jenkins.model.*
import hudson.model.*

def jenkinsInstance = Jenkins.instance

// Cancel all pending jobs in the queue
jenkinsInstance.queue.items.each { queueItem ->
    println "Canceling queued job: ${queueItem.task.name}"
    jenkinsInstance.queue.cancel(queueItem.task)
}

// Abort all running jobs
jenkinsInstance.getAllItems(Job.class).each { job ->
    job.builds.each { build ->
        if (build.isBuilding()) {
            println "Aborting running job: ${build.displayName} in ${job.name}"
            build.doStop()
        }
    }
}

println "All running and queued jobs have been terminated."