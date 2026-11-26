package io.github.haasfynn.navy.effects

import com.intellij.execution.ExecutionListener
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.task.ProjectTaskListener
import com.intellij.task.ProjectTaskManager
import io.github.haasfynn.navy.Navy
import io.github.haasfynn.navy.settings.NavySettings

private fun ping(project: Project, success: Boolean) {
    ApplicationManager.getApplication().invokeLater {
        if (!project.isDisposed && NavySettings.sonarPing && Navy.effectsAllowed()) {
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
