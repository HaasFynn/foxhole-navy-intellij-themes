package io.github.haasfynn.wardennavy.effects

import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener

/** Gives every newly opened editor its bubble layer and removes it again when the editor closes. */
class BubbleEditorListener : EditorFactoryListener {
    override fun editorCreated(event: EditorFactoryEvent) = Bubbles.editorCreated(event.editor)

    override fun editorReleased(event: EditorFactoryEvent) = Bubbles.editorReleased(event.editor)
}
