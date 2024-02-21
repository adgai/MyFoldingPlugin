package com.example.myinlayhints

import ai.grazie.utils.chainIfNotNull
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.openapi.editor.FoldingModel
import com.intellij.openapi.editor.ex.FoldingModelEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.editor.ex.util.EditorUtil
import com.intellij.openapi.editor.impl.FoldingModelImpl

class FileOpenListener : FileEditorManagerListener {

    private val foldingBuilder: MyFoldingBuilder = MyFoldingBuilder()

    override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
        // 在文件打开时触发构建代码折叠区域的逻辑
        val project: Project? = source.project
        project?.let {
            // 获取文件的 PsiElement 根节点
            val psiFile: PsiFile? = PsiManager.getInstance(it).findFile(file)
            psiFile?.let { psiFile ->
                // 调用代码折叠逻辑
                buildFoldingDescriptors(psiFile, it)
            }
        }
    }

    // 构建代码折叠区域的描述符的逻辑
    private fun buildFoldingDescriptors(psiFile: PsiFile, project: Project) {
        // 获取文件的 Document 对象
        val document: Document? = PsiDocumentManager.getInstance(project).getDocument(psiFile)
        document?.let { document ->
            // 调用代码折叠逻辑
            val descriptors: Array<FoldingDescriptor> = foldingBuilder.buildFoldRegions(psiFile.node, document)

            // 获取 FoldingModel 并添加描述符
            val editor = FileEditorManager.getInstance(project).getSelectedTextEditor()
            val foldingModel: FoldingModelEx = editor?.foldingModel as FoldingModelEx
            foldingModel.runBatchFoldingOperation {
                val foldingGroup = FoldingGroup.newGroup("myGroup") // 创建 FoldingGroup
                descriptors.forEach {
                    foldingModel.createFoldRegion(
                        it.range.startOffset-1,
                        it.range.endOffset+10,
                        "1234222",
                        foldingGroup,false
                    )
//                    var l = foldingModel as FoldingModelImpl
//                    l.collapseFoldRegion()
                }
                // 重建代码折叠模型
//                EditorUtil.allowTreeAccessForFile(editor.document)
//                foldingModel.rebuild()
            }
        }
    }
}
