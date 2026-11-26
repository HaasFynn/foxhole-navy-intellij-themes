package io.github.haasfynn.navy.instruments

import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.openapi.wm.impl.status.EditorBasedWidget
import com.intellij.openapi.wm.impl.status.widget.StatusBarWidgetsManager
import io.github.haasfynn.navy.Navy
import java.awt.Component

/** Adds the depth gauge to the status bar while a Warden Navy theme is active. */
class DepthGaugeWidgetFactory : StatusBarWidgetFactory {
    override fun getId() = ID

    override fun getDisplayName() = "Depth Gauge"

    override fun isAvailable(project: Project) = Navy.variant() != null

    override fun createWidget(project: Project): StatusBarWidget = DepthGauge(project)

    companion object {
        const val ID = "NavyDepthGauge"

        /** Shows or hides the gauge in every open project after a theme change. */
        fun refresh() {
            for (project in ProjectManager.getInstance().openProjects) {
                if (project.isDisposed) continue
                project.getService(StatusBarWidgetsManager::class.java)?.updateWidget(DepthGaugeWidgetFactory::class.java)
            }
        }
    }
}

/** Shows how deep the caret is in the file: one metre per line. */
private class DepthGauge(project: Project) : EditorBasedWidget(project), StatusBarWidget.TextPresentation {
    override fun ID() = DepthGaugeWidgetFactory.ID

    override fun getPresentation(): StatusBarWidget.WidgetPresentation = this

    override fun install(statusBar: StatusBar) {
        super.install(statusBar)
        EditorFactory.getInstance().eventMulticaster.addCaretListener(object : CaretListener {
            override fun caretPositionChanged(event: CaretEvent) {
                if (isOurEditor(event.editor)) update()
            }
        }, this)
        project.messageBus.connect(this).subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER,
            object : FileEditorManagerListener {
                override fun selectionChanged(event: FileEditorManagerEvent) = update()
            })
    }

    private fun update() {
        myStatusBar?.updateWidget(ID())
    }

    private fun position(): Pair<Int, Int>? {
        val editor = getEditor() ?: return null
        return editor.caretModel.logicalPosition.line + 1 to editor.document.lineCount
    }

    override fun getText(): String = position()?.let { (line, _) -> "−$line m" } ?: ""

    override fun getTooltipText(): String? =
        position()?.let { (line, total) -> "Depth gauge: line $line of $total" }

    override fun getAlignment() = Component.CENTER_ALIGNMENT
}
