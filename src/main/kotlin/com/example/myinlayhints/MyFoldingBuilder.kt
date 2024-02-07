package com.example.myinlayhints

import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilder
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.PsiModifier
import com.intellij.psi.PsiReferenceExpression
import org.apache.commons.lang3.StringUtils
import org.jetbrains.uast.util.isInstanceOf


class MyFoldingBuilder : FoldingBuilder {

    lateinit var apolloClient: ApolloOpenApiClient


    override fun buildFoldRegions(node: ASTNode, document: Document): Array<FoldingDescriptor> {
        val descriptors = mutableListOf<FoldingDescriptor>()
        val root = node.psi
        val project = root.project

        val settings = MyPluginProjectSettings.getInstance(project)
        var splitMethodSignatureList = settings.methodSignature.split(",").filter { s -> StringUtils.isNotBlank(s) }
        if (splitMethodSignatureList.size == 0) {
            splitMethodSignatureList = arrayListOf(
                "configUtils.getBool",
                "configUtils.getBoolean",
                "configUtils.getInt",
                "configUtils.getLong"
            );
        }


        // 使用递归访问者遍历 PSI 树查找方法调用
        root.accept(object : JavaRecursiveElementVisitor() {
            override fun visitMethodCallExpression(expression: PsiMethodCallExpression) {
                super.visitMethodCallExpression(expression)

                // 检查方法调用是否符合我们的条件（即调用指定的类和方法）
                if (splitMethodSignatureList.contains(expression.methodExpression.qualifiedName)) {
                    // 创建一个 FoldingDescriptor 并添加到列表中
                    val range = expression.textRange
                    descriptors.add(FoldingDescriptor(expression.node, range))
                }

//                val qualifiedName = expression.resolveMethod()?.containingClass?.qualifiedName
//                if (){
//
//                }

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
        val element = node.psi

        val project = element.project
        val settings = MyPluginProjectSettings.getInstance(project)

        if (element is PsiMethodCallExpression) {
            val methodCallExpression = element

            val argumentList = methodCallExpression.argumentList
            val arguments = argumentList.expressions

            val staticConstantValue = getStaticConstantValue(arguments[0])
            val defaultValue = getBooleanValue(arguments[1])


            val token = settings.token
            val url = settings.url
            val serviceName = settings.serviceName

            val initialized = ::apolloClient.isInitialized
            if (!initialized) {
                apolloClient = ApolloOpenApiClient.newBuilder().withToken(token).withPortalUrl(url).build()
            }


            val apolloValue =
                apolloClient.getItem(
                    serviceName,
                    "dev",
                    "default",
                    "application",
                    staticConstantValue.toString()
                )?.value


            if (apolloValue == null) {
                return defaultValue.toString()
            } else {
                return apolloValue.toString()
            }


        }


        return "xxxxxxxxxxxxxxxxxxx"
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return true
    }

    fun getStaticConstantValue(ele: PsiElement): Any? {
        if (ele is PsiReferenceExpression) {
            val resolve = ele.resolve()
            if (resolve is PsiField) {
                val field = resolve;

                if (field.hasModifierProperty(PsiModifier.STATIC) && field.hasModifierProperty(PsiModifier.FINAL)) {
                    return field.computeConstantValue()
                }
            }

        }

        return null
    }

    fun getBooleanValue(ele: PsiElement): Any? {
        if (ele is PsiLiteralExpression) {
            val value = ele.value

            return value
        }

        return null
    }
}
