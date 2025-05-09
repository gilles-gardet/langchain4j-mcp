package com.ggardet.mcp.ui

import com.ggardet.mcp.service.ChatService
import com.vaadin.flow.component.Key.ENTER
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.notification.NotificationVariant
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.dom.Style
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import org.apache.commons.lang3.StringUtils

private const val backgroundColor = "#f9f9f9"

@Route(StringUtils.EMPTY)
@PageTitle("LLM Chat")
class ChatView(private val chatService: ChatService) : VerticalLayout() {
    private val chatHistory = Div()
    private val userInput = TextField("Your message")
    private val sendButton = Button("Send")
    private val urlInput = TextField("URL to ingest")
    private val ingestButton = Button("Ingest Content")

    init {
        setupUI()
        setupEventHandlers()
    }

    private fun setupUI() {
        setupGeneralElements()
        setupUrlInput()
        setupChatContainer()
    }

    private fun setupGeneralElements() {
        setSizeFull()
        isPadding = true
        add(H1("Chat with LLM"))
    }

    private fun setupChatContainer() {
        chatHistory.apply {
            className = "chat-history"
            style.setBorder("1px solid #ccc")
            style.setOverflow(Style.Overflow.AUTO)
            style.setHeight("60vh")
            style.setWidth("100%")
            style.setMarginBottom("10px")
            style.setBackgroundColor(backgroundColor)
        }
        add(chatHistory)
        userInput.apply {
            placeholder = "Type your message here..."
            isClearButtonVisible = true
            width = "100%"
        }
        sendButton.apply {
            addThemeVariants(ButtonVariant.LUMO_PRIMARY)
            style.setAlignSelf(Style.AlignSelf.FLEX_END)
        }
        val inputLayout = HorizontalLayout(userInput, sendButton)
        inputLayout.width = "100%"
        inputLayout.expand(userInput)
        add(inputLayout)
    }

    private fun setupUrlInput() {
        urlInput.apply {
            placeholder = "Enter URL to ingest content..."
            isClearButtonVisible = true
            width = "100%"
        }
        ingestButton.apply {
            addThemeVariants(ButtonVariant.LUMO_SUCCESS)
            style.setAlignSelf(Style.AlignSelf.FLEX_END)
        }
        val urlLayout = HorizontalLayout(urlInput, ingestButton)
        urlLayout.width = "100%"
        urlLayout.expand(urlInput)
        add(urlLayout)
    }

    private fun setupEventHandlers() {
        sendButton.addClickListener { sendMessage() }
        userInput.addKeyPressListener { if (it.key == ENTER) sendMessage() }
        ingestButton.addClickListener { ingestContent() }
    }

    private fun ingestContent() {
        val url = urlInput.value.trim()
        if (url.isEmpty()) {
            val notification = Notification("Please enter a URL to ingest", 3000, Notification.Position.BOTTOM_CENTER)
            notification.addThemeVariants(NotificationVariant.LUMO_WARNING)
            notification.open()
        }
        try {
            chatService.ingestContentFromUrl(url)
            val notification =
                Notification("Content from URL successfully ingested!", 3000, Notification.Position.BOTTOM_CENTER)
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS)
            notification.open()
            urlInput.clear()
            addMessageToChat(
                "System",
                "Content from URL '$url' has been ingested and is now available for context in your questions.",
                "system-message"
            )
        } catch (exception: Exception) {
            val notification =
                Notification(
                    "Failed to ingest content: ${exception.message}",
                    3000,
                    Notification.Position.BOTTOM_CENTER
                )
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR)
            notification.open()
        }
    }

    private fun sendMessage() {
        val message = userInput.value.trim()
        if (message.isNotEmpty()) {
            addMessageToChat("You", message, "user-message")
            userInput.clear()
            userInput.focus()
            val response = chatService.sendMessage(message)
            addMessageToChat("LLM", response, "llm-message")
        }
    }

    private fun addMessageToChat(sender: String, message: String, className: String) {
        val messageDiv = Div()
        messageDiv.className = className
        messageDiv.style.setMarginBottom("10px")
        messageDiv.style.setPadding("8px")
        messageDiv.style.setBorderRadius("5px")
        when (className) {
            "user-message" -> {
                messageDiv.style.setBackgroundColor("#e1f5fe")
                messageDiv.style.setAlignSelf(Style.AlignSelf.FLEX_END)
            }
            "system-message" -> {
                messageDiv.style.setBackgroundColor("#e8f5e9")
                messageDiv.style.setAlignSelf(Style.AlignSelf.CENTER)
            }
            else -> {
                messageDiv.style.setBackgroundColor("#f1f1f1")
                messageDiv.style.setAlignSelf(Style.AlignSelf.FLEX_START)
            }
        }
        val senderSpan = Span("$sender: ")
        senderSpan.style.setFontWeight("bold")
        val messageSpan = Span(message)
        messageDiv.add(senderSpan, messageSpan)
        chatHistory.add(messageDiv)
        ui.ifPresent { ui ->
            ui.access {
                ui.page.executeJs("document.querySelector('.chat-history').scrollTop = document.querySelector('.chat-history').scrollHeight")
            }
        }
    }
}
