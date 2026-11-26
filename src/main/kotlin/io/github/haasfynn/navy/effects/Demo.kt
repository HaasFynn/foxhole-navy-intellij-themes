package io.github.haasfynn.navy.effects

import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.EditorKind
import com.intellij.openapi.project.ProjectManager
import javax.swing.Timer

/**
 * For previews and screenshots: `-Dnavy.demo=bubbles,torpedo,ping` replays the named
 * effects every few seconds without anyone typing, deleting or building.
 */
internal object Demo {
    private var round = 0

    fun startIfRequested() {
        val wanted = System.getProperty("navy.demo")?.split(',')?.map(String::trim)?.filter(String::isNotEmpty)
        if (wanted.isNullOrEmpty()) return
        Timer(3000) {
            val editor = EditorFactory.getInstance().allEditors
                .firstOrNull { it.editorKind == EditorKind.MAIN_EDITOR && it.contentComponent.isShowing }
            when (wanted[round++ % wanted.size]) {
                "bubbles" -> editor?.let(EditorEffects::bubbles)
                "torpedo" -> editor?.let(EditorEffects::torpedo)
                "ping" -> ProjectManager.getInstance().openProjects.firstOrNull()?.let { SonarPing.play(it, round % 4 < 2) }
            }
        }.start()
    }
}
