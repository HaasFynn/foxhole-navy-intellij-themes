package io.github.haasfynn.wardennavy.effects

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.ex.AnActionListener
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorKind
import io.github.haasfynn.wardennavy.WardenNavy
import io.github.haasfynn.wardennavy.settings.WardenNavySettings
import java.lang.ref.WeakReference
import javax.swing.Timer

/**
 * Effects that answer what you do in the editor: bubbles rise from the caret when you pause
 * typing, and a torpedo strikes a line you delete.
 */
class EditorEffects : AnActionListener {

    override fun afterEditorTyping(c: Char, dataContext: DataContext) {
        if (!WardenNavySettings.bubbles) return
        val editor = CommonDataKeys.EDITOR.getData(dataContext) ?: return
        typedIn = WeakReference(editor)
        typingPause.restart()
    }

    override fun beforeActionPerformed(action: AnAction, event: AnActionEvent) {
        if (!WardenNavySettings.torpedo || ActionManager.getInstance().getId(action) != DELETE_LINE) return
        val editor = event.getData(CommonDataKeys.EDITOR) ?: return
        torpedo(editor)
    }

    companion object {
        private const val DELETE_LINE = "EditorDeleteLine"
        private const val PAUSE_MILLIS = 450
        private const val MIN_GAP_NANOS = 1_500_000_000L

        private var typedIn = WeakReference<Editor>(null)
        private var lastBubbles = 0L
        private val typingPause = Timer(PAUSE_MILLIS) { typedIn.get()?.let(::bubbles) }.apply { isRepeats = false }

        private fun usable(editor: Editor) =
            WardenNavy.effectsAllowed() && !editor.isDisposed && editor.editorKind == EditorKind.MAIN_EDITOR &&
                editor.contentComponent.isShowing

        /** Lets a few bubbles rise from the caret, but not more often than every second or so. */
        internal fun bubbles(editor: Editor) {
            val now = System.nanoTime()
            if (!usable(editor) || now - lastBubbles < MIN_GAP_NANOS) return
            val area = editor.scrollingModel.visibleArea
            val caret = editor.visualPositionToXY(editor.caretModel.visualPosition)
            val x = caret.x - area.x
            val y = caret.y - area.y
            if (x < 0 || x > area.width || y < 0 || y > area.height) return
            lastBubbles = now
            Effects.play(BubbleBurst(editor, x.toDouble(), y.toDouble()))
        }

        internal fun torpedo(editor: Editor) {
            if (!usable(editor)) return
            val area = editor.scrollingModel.visibleArea
            val caret = editor.visualPositionToXY(editor.caretModel.visualPosition)
            val y = caret.y - area.y
            if (y < 0 || y > area.height) return
            // It comes in from the right edge of the editor and runs the whole line to the left.
            Effects.play(TorpedoStrike(editor, y, editor.lineHeight, area.width.toDouble()))
        }
    }
}
