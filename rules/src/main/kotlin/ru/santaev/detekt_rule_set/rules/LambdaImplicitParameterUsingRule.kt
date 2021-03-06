package ru.santaev.detekt_rule_set.rules

import io.gitlab.arturbosch.detekt.api.*
import org.jetbrains.kotlin.psi.*
import ru.santaev.detekt_rule_set.utils.IMPLICIT_PARAMETER_NAME
import ru.santaev.detekt_rule_set.utils.line

class LambdaImplicitParameterUsingRule : Rule() {

    private val implicitParameterCounter = ImplicitParameterCounter()

    override val issue = Issue(
        id = javaClass.simpleName,
        severity = Severity.CodeSmell,
        description = "Use explicit parameter name of lambda",
        debt = Debt.FIVE_MINS
    )

    override fun visitLambdaExpression(lambdaExpression: KtLambdaExpression) {
        super.visitLambdaExpression(lambdaExpression)
        val hasSpecifiedParameter = lambdaExpression.functionLiteral.hasParameterSpecification()
        val implicitParameterUsingCount = implicitParameterCounter.calculate(lambdaExpression)
        if (isMultiline(lambdaExpression) &&
            hasSpecifiedParameter.not() &&
            implicitParameterUsingCount > 0
        ) {
            report(createReport(lambdaExpression))
        }
    }

    private fun isMultiline(lambdaExpression: KtLambdaExpression): Boolean {
        val lBraceLine = lambdaExpression.leftCurlyBrace.line(lambdaExpression.containingKtFile)
        val rBraceLine = lambdaExpression.rightCurlyBrace?.line(lambdaExpression.containingKtFile)
        return rBraceLine != lBraceLine
    }

    private fun createReport(lambdaExpression: KtLambdaExpression): Finding {
        return CodeSmell(
            issue = issue,
            entity = Entity.from(lambdaExpression),
            message = "Use explicit parameter name of lambda"
        )
    }

    private class ImplicitParameterCounter: KtTreeVisitorVoid() {

        private var count: Int = 0
        private var rootElement: KtElement? = null

        override fun visitReferenceExpression(expression: KtReferenceExpression) {
            super.visitReferenceExpression(expression)
            if (expression.text == IMPLICIT_PARAMETER_NAME) {
                count++
            }
        }

        override fun visitLambdaExpression(lambdaExpression: KtLambdaExpression) {
            if (rootElement == lambdaExpression) {
                super.visitLambdaExpression(lambdaExpression)
            }
        }

        fun calculate(expression: KtElement): Int {
            count = 0
            rootElement = expression
            expression.accept(this)
            return count
        }
    }
}