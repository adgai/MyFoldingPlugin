package com.example.myinlayhints

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethodCallExpression


class MyFoldingBuilder : FoldingBuilderEx() {
    override fun buildFoldRegions(p0: PsiElement, p1: Document, p2: Boolean): Array<FoldingDescriptor> {
        return doBuildFoldRegions1(p0.node, p1)
    }


    fun doBuildFoldRegions1(node: ASTNode, document: Document): Array<FoldingDescriptor> {
        val descriptors = mutableListOf<FoldingDescriptor>()
        val root = node.psi

        // 使用递归访问者遍历 PSI 树查找方法调用
        root.accept(object : JavaRecursiveElementVisitor() {
            override fun visitMethodCallExpression(expression: PsiMethodCallExpression) {
                super.visitMethodCallExpression(expression)

                // 检查方法调用是否符合我们的条件（即调用指定的类和方法）
                if ("getBoolean".equals(expression.methodExpression.qualifiedName)) {
                    // 创建一个 FoldingDescriptor 并添加到列表中
                    val range = expression.textRange
                    descriptors.add(
                        FoldingDescriptor(
                            node,
                            range,
                            null,
                            setOf<Any>(expression),
                            false,
                            "testvalue",
                            true
                        )
                    )
                }

            }
        })

        return descriptors.toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode): String? {
        return null
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return true
    }

}
