package br.edu.iftm.workouttracker.ui.exercicio

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import br.edu.iftm.workouttracker.R
import br.edu.iftm.workouttracker.util.Formatadores

data class PontoGrafico(
    val rotulo: String,
    val valor: Double
)

class GraficoCargaView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val densidade = resources.displayMetrics.density

    private val larguraMaximaBarra = 24f * densidade
    private val raioTopo = 4f * densidade
    private val espacoRotuloTopo = 22f * densidade
    private val espacoRotuloBase = 20f * densidade
    private val alturaMinimaBarra = 2f * densidade

    private val corBarra = context.getColor(R.color.positive)

    private val pintaBarra = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = corBarra
    }

    private val pintaBase = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.divider)
        strokeWidth = 1f * densidade
    }

    private val pintaValor = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.text_primary)
        textSize = sp(11f)
        typeface = Typeface.MONOSPACE
        textAlign = Paint.Align.CENTER
    }

    private val pintaRotulo = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.text_tertiary)
        textSize = sp(10f)
        typeface = Typeface.MONOSPACE
        textAlign = Paint.Align.CENTER
    }

    private val caminho = Path()
    private val retangulo = RectF()

    private var pontos: List<PontoGrafico> = emptyList()
    private var selecionado: Int = -1

    fun definirPontos(novos: List<PontoGrafico>) {
        pontos = novos.takeLast(MAXIMO_BARRAS)
        selecionado = pontos.lastIndex
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (pontos.isEmpty()) return

        val esquerda = paddingLeft.toFloat()
        val direita = (width - paddingRight).toFloat()
        val topo = paddingTop + espacoRotuloTopo
        val base = height - paddingBottom - espacoRotuloBase
        val alturaUtil = base - topo
        val larguraFaixa = (direita - esquerda) / pontos.size
        val larguraBarra = minOf(larguraMaximaBarra, larguraFaixa - 2f * densidade)
        val maximo = pontos.maxOf { it.valor }.takeIf { it > 0 } ?: 1.0

        canvas.drawLine(esquerda, base, direita, base, pintaBase)

        pontos.forEachIndexed { indice, ponto ->
            val centro = esquerda + larguraFaixa * indice + larguraFaixa / 2f
            val proporcao = (ponto.valor / maximo).toFloat()
            val altura = maxOf(alturaMinimaBarra, alturaUtil * proporcao)
            val topoBarra = base - altura

            pintaBarra.alpha = if (indice == selecionado) 255 else 115
            retangulo.set(centro - larguraBarra / 2f, topoBarra, centro + larguraBarra / 2f, base)
            val raio = minOf(raioTopo, altura)
            caminho.reset()
            caminho.addRoundRect(
                retangulo,
                floatArrayOf(raio, raio, raio, raio, 0f, 0f, 0f, 0f),
                Path.Direction.CW
            )
            canvas.drawPath(caminho, pintaBarra)

            val yRotulo = base + espacoRotuloBase - 6f * densidade
            canvas.drawText(ponto.rotulo, centro, yRotulo, pintaRotulo)

            if (indice == selecionado) {
                val texto = context.getString(R.string.label_kg_format, Formatadores.numero(ponto.valor))
                canvas.drawText(texto, centro, topoBarra - 6f * densidade, pintaValor)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (pontos.isEmpty()) return false
        when (event.action) {
            MotionEvent.ACTION_DOWN -> return true
            MotionEvent.ACTION_UP -> {
                val larguraFaixa = (width - paddingLeft - paddingRight).toFloat() / pontos.size
                val indice = ((event.x - paddingLeft) / larguraFaixa).toInt().coerceIn(0, pontos.lastIndex)
                if (indice != selecionado) {
                    selecionado = indice
                    invalidate()
                }
                performClick()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun sp(valor: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, valor, resources.displayMetrics)

    companion object {
        private const val MAXIMO_BARRAS = 8
    }
}
