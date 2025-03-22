package ru.netology.nmedia.entity

import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.AttachmentType

class AttachmentEmbedded(
    val url: String,
    val type: AttachmentType,
) {

    companion object {
        fun fromDto(attachment: Attachment) = with(attachment) {
            AttachmentEmbedded(url, type)
        }
    }

    fun toDto(): Attachment = Attachment(url, type)

}