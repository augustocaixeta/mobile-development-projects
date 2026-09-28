package br.edu.iftm.readingmanager.ui.detail

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.edu.iftm.readingmanager.R
import br.edu.iftm.readingmanager.data.Note
import br.edu.iftm.readingmanager.data.NoteType
import br.edu.iftm.readingmanager.ui.components.CardDialog
import br.edu.iftm.readingmanager.ui.components.FieldLabel
import br.edu.iftm.readingmanager.ui.components.PlainButton
import br.edu.iftm.readingmanager.ui.components.PrimaryButton
import br.edu.iftm.readingmanager.ui.components.SegmentedControl
import br.edu.iftm.readingmanager.ui.components.bottomBorder
import br.edu.iftm.readingmanager.ui.theme.ReadingTheme
import br.edu.iftm.readingmanager.util.Formats
import br.edu.iftm.readingmanager.util.toLocalDateTime
import java.util.Locale

private const val NoteMaxLength = 1_000

/**
 * Seção do diário de leitura no detalhe: cabeçalho com a contagem, as notas e o botão de nova nota.
 *
 * @param notes notas do livro, das mais recentes para as mais antigas.
 * @param onAdd abre o editor para uma nota nova.
 * @param onOpen abre o editor com a nota tocada.
 */
@Composable
fun NotesSection(notes: List<Note>, onAdd: () -> Unit, onOpen: (Note) -> Unit) {
    val colors = ReadingTheme.colors
    val typography = ReadingTheme.typography
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 28.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = stringResource(R.string.notes_header), style = typography.overline, color = colors.text3)
        Text(
            text = pluralStringResource(R.plurals.notes_count, notes.size, notes.size),
            style = typography.caption,
            color = colors.text2
        )
    }
    if (notes.isEmpty()) {
        Text(
            text = stringResource(R.string.notes_empty),
            modifier = Modifier.padding(top = 12.dp),
            style = typography.caption,
            color = colors.text3
        )
    }
    notes.forEach { note ->
        NoteRow(note = note, onClick = { onOpen(note) })
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(role = Role.Button, onClick = onAdd),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(text = stringResource(R.string.action_add_note), style = typography.bodyMedium, color = colors.text2)
    }
}

/**
 * Linha de uma nota, no estilo das linhas de lembrete do Figma: ícone do tipo, texto e data em mono.
 *
 * @param note nota exibida.
 * @param onClick abre a nota para edição.
 */
@Composable
private fun NoteRow(note: Note, onClick: () -> Unit) {
    val colors = ReadingTheme.colors
    val typography = ReadingTheme.typography
    val kind = stringResource(noteTypeLong(note.type))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            painter = painterResource(noteTypeIcon(note.type)),
            contentDescription = null,
            modifier = Modifier
                .padding(top = 1.dp)
                .size(18.dp),
            tint = noteTypeTint(note.type)
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = note.text,
                style = typography.body,
                color = colors.text,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = note.page?.let { page ->
                    stringResource(R.string.pair_format, kind, stringResource(R.string.note_page_format, Formats.integer(page)))
                } ?: kind,
                style = typography.caption,
                color = colors.text3,
                maxLines = 1
            )
        }
        Text(text = noteDate(note.createdAt), style = typography.monoSmall, color = colors.text2)
    }
}

/**
 * Editor de nota em cartão, com o tipo, o texto e as ações de salvar, excluir e cancelar.
 *
 * @param note nota em edição, ou null para uma nota nova.
 * @param onDismiss fecha o editor sem salvar.
 * @param onSave recebe o tipo e o texto já validados.
 * @param onDelete exclui a nota em edição.
 */
@Composable
fun NoteDialog(
    note: Note?,
    onDismiss: () -> Unit,
    onSave: (NoteType, String) -> Unit,
    onDelete: () -> Unit
) {
    var type by rememberSaveable { mutableStateOf(note?.type ?: NoteType.ANNOTATION) }
    var text by rememberSaveable { mutableStateOf(note?.text.orEmpty()) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val requiredMessage = stringResource(R.string.error_note_required)
    CardDialog(
        title = stringResource(if (note == null) R.string.note_dialog_new else R.string.note_dialog_edit),
        onDismiss = onDismiss
    ) {
        SegmentedControl(
            labels = NoteType.entries.map { stringResource(noteTypeShort(it)) },
            selectedIndex = type.ordinal,
            onSelect = { index -> type = NoteType.entries[index] }
        )
        NoteTextArea(
            label = stringResource(R.string.note_text_label),
            value = text,
            onValueChange = {
                text = it
                error = null
            },
            placeholder = stringResource(R.string.note_text_placeholder),
            error = error
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            PrimaryButton(
                text = stringResource(R.string.action_save),
                onClick = {
                    val trimmed = text.trim()
                    if (trimmed.isEmpty()) {
                        error = requiredMessage
                    } else {
                        onSave(type, trimmed)
                    }
                }
            )
            if (note != null) {
                PlainButton(text = stringResource(R.string.action_delete_note), onClick = onDelete)
            }
            PlainButton(text = stringResource(R.string.action_cancel), onClick = onDismiss)
        }
    }
}

/**
 * Campo de texto de várias linhas com o mesmo visual dos campos de linha do Figma.
 *
 * @param label rótulo acima do texto.
 * @param value texto atual do campo.
 * @param onValueChange recebe o texto digitado, limitado ao tamanho máximo da nota.
 * @param placeholder texto mostrado com o campo vazio.
 * @param error mensagem exibida no lugar do rótulo quando o texto é inválido.
 */
@Composable
private fun NoteTextArea(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    error: String?
) {
    val colors = ReadingTheme.colors
    val typography = ReadingTheme.typography
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .bottomBorder(if (error != null) colors.accent else colors.line)
            .padding(top = 4.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        FieldLabel(label = label, error = error)
        BasicTextField(
            value = value,
            onValueChange = { input -> onValueChange(input.take(NoteMaxLength)) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = typography.body.copy(color = colors.text),
            cursorBrush = SolidColor(colors.accent),
            minLines = 3,
            maxLines = 8,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty()) {
                        Text(text = placeholder, style = typography.body, color = colors.text3)
                    }
                    inner()
                }
            }
        )
    }
}

/**
 * Nome curto do tipo de nota, usado no controle segmentado.
 *
 * @param type tipo da nota.
 * @return recurso de texto como Insight.
 */
@StringRes
private fun noteTypeShort(type: NoteType): Int = when (type) {
    NoteType.ANNOTATION -> R.string.note_type_annotation
    NoteType.INSIGHT -> R.string.note_type_insight
    NoteType.QUOTE -> R.string.note_type_quote
}

/**
 * Nome completo do tipo de nota, usado abaixo do texto da nota.
 *
 * @param type tipo da nota.
 * @return recurso de texto como Citação favorita.
 */
@StringRes
private fun noteTypeLong(type: NoteType): Int = when (type) {
    NoteType.ANNOTATION -> R.string.note_type_annotation
    NoteType.INSIGHT -> R.string.note_type_insight
    NoteType.QUOTE -> R.string.note_type_quote_long
}

/**
 * Ícone que identifica o tipo de nota na lista.
 *
 * @param type tipo da nota.
 * @return recurso de imagem do Material Symbols.
 */
@DrawableRes
private fun noteTypeIcon(type: NoteType): Int = when (type) {
    NoteType.ANNOTATION -> R.drawable.ic_history_edu
    NoteType.INSIGHT -> R.drawable.ic_psychology
    NoteType.QUOTE -> R.drawable.ic_favorite
}

/**
 * Cor do ícone de cada tipo de nota.
 *
 * @param type tipo da nota.
 * @return cor do tema usada no ícone.
 */
@Composable
private fun noteTypeTint(type: NoteType): Color = when (type) {
    NoteType.ANNOTATION -> ReadingTheme.colors.text3
    NoteType.INSIGHT -> ReadingTheme.colors.positive
    NoteType.QUOTE -> ReadingTheme.colors.text2
}

/**
 * Formata o momento em que a nota foi escrita.
 *
 * @param millis instante em milissegundos.
 * @return texto como 29/09 09:00.
 */
private fun noteDate(millis: Long): String {
    val dateTime = millis.toLocalDateTime()
    return "%02d/%02d %s".format(Locale.forLanguageTag("pt-BR"), dateTime.dayOfMonth, dateTime.monthValue, Formats.time(dateTime.toLocalTime()))
}
