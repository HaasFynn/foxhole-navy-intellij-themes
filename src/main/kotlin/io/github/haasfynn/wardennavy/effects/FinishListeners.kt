package io.github.haasfynn.wardennavy.effects

import com.intellij.execution.ExecutionListener
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.task.ProjectTaskListener
import com.intellij.task.ProjectTaskManager
import io.github.haasfynn.wardennavy.WardenNavy
import io.github.haasfynn.wardennavy.settings.WardenNavySettings

private fun ping(project: Project, success: Boolean) {
    ApplicationManager.getApplication().invokeLater {
        if (!project.isDisposed && WardenNavySettings.sonarPing && WardenNavy.effectsAllowed()) {
            SonarPing.play(project, success)
        }
    }
}

/** Pings the sonar when a build finishes. */
class BuildFinishListener(private val project: Project) : ProjectTaskListener {
    override fun finished(result: ProjectTaskManager.Result) {
        if (!result.isAborted) ping(project, !result.hasErrors())
    }
}

/** Pings the sonar when a run configuration ends, tests included: green for exit code 0, red otherwise. */
class RunFinishListener(private val project: Project) : ExecutionListener {
    override fun processTerminated(executorId: String, env: ExecutionEnvironment, handler: ProcessHandler, exitCode: Int) {
        ping(project, exitCode == 0)
    }
}
