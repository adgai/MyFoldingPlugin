package com.example.myinlayhints

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilder
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiMethodCallExpression


class MyFoldingBuilder : FoldingBuilder {

    override fun buildFoldRegions(node: ASTNode, document: Document): Array<FoldingDescriptor> {
        val descriptors = mutableListOf<FoldingDescriptor>()
        val root = node.psi

        // 使用递归访问者遍历 PSI 树查找方法调用
        root.accept(object : JavaRecursiveElementVisitor() {
            override fun visitMethodCallExpression(expression: PsiMethodCallExpression) {
                super.visitMethodCallExpression(expression)

                // 检查方法调用是否符合我们的条件（即调用指定的类和方法）
                if (expression.methodExpression.qualifiedName == "System.out.println") {
                    // 创建一个 FoldingDescriptor 并添加到列表中
                    val range = expression.textRange
                    descriptors.add(FoldingDescriptor(expression.node, range))
                }

                val qualifiedName = expression.resolveMethod()?.containingClass?.qualifiedName
                if (){

                }

            }
        })

        // Kotlin 代码的处理
//        PsiTreeUtil.collectElementsOfType(root, KtCallExpression::class.java).forEach { callExpression ->
//            if (callExpression.calleeExpression?.text == "yourMethod" /* 需要更详细的检查来确认类名 */) {
//                val range = callExpression.textRange
//                descriptors.add(FoldingDescriptor(callExpression.node, range))
//            }
//        }

        return descriptors.toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode): String? {
        return "xxxxxxxxxxxxxxxxxxx"
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return true
    }
}
