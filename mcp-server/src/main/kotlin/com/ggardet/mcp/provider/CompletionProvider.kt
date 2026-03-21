package com.ggardet.mcp.provider

import com.ggardet.mcp.repository.PeopleRepository
import io.modelcontextprotocol.spec.McpSchema.CompleteRequest
import io.modelcontextprotocol.spec.McpSchema.CompleteResult
import io.modelcontextprotocol.spec.McpSchema.CompleteResult.CompleteCompletion
import org.springaicommunity.mcp.annotation.McpComplete
import org.springframework.stereotype.Component

@Component
class CompletionProvider(private val peopleRepository: PeopleRepository) {

    @McpComplete(uri = "people://{name}/profile")
    fun completeResourceName(name: String): List<String> {
        val prefix = name.lowercase()
        return peopleRepository.findAll()
            .map { it.name }
            .filter { it.lowercase().startsWith(prefix) }
    }

    @McpComplete(prompt = "people-by-name")
    fun completeName(name: String): List<String> {
        val prefix = name.lowercase()
        return peopleRepository.findAll()
            .map { it.name }
            .filter { it.lowercase().startsWith(prefix) }
    }

    @McpComplete(prompt = "people-by-country")
    fun completeCountry(country: String): List<String> {
        val prefix = country.lowercase()
        return peopleRepository.findAll()
            .map { it.country }
            .distinct()
            .filter { it.lowercase().startsWith(prefix) }
    }

    @McpComplete(prompt = "weather-lookup")
    fun completeCountryCode(request: CompleteRequest): CompleteResult {
        val countryCodes = listOf(
            "AD", "AE", "AF", "AG", "AL", "AM", "AO", "AR", "AT", "AU",
            "AZ", "BA", "BB", "BD", "BE", "BF", "BG", "BH", "BI", "BJ",
            "BN", "BO", "BR", "BS", "BT", "BW", "BY", "BZ", "CA", "CD",
            "CF", "CG", "CH", "CI", "CL", "CM", "CN", "CO", "CR", "CU",
            "CV", "CY", "CZ", "DE", "DJ", "DK", "DM", "DO", "DZ", "EC",
            "EE", "EG", "ER", "ES", "ET", "FI", "FJ", "FM", "FR", "GA",
            "GB", "GD", "GE", "GH", "GM", "GN", "GQ", "GR", "GT", "GW",
            "GY", "HN", "HR", "HT", "HU", "ID", "IE", "IL", "IN", "IQ",
            "IR", "IS", "IT", "JM", "JO", "JP", "KE", "KG", "KH", "KI",
            "KM", "KN", "KP", "KR", "KW", "KZ", "LA", "LB", "LC", "LI",
            "LK", "LR", "LS", "LT", "LU", "LV", "LY", "MA", "MC", "MD",
            "ME", "MG", "MH", "MK", "ML", "MM", "MN", "MR", "MT", "MU",
            "MV", "MW", "MX", "MY", "MZ", "NA", "NE", "NG", "NI", "NL",
            "NO", "NP", "NR", "NZ", "OM", "PA", "PE", "PG", "PH", "PK",
            "PL", "PT", "PW", "PY", "QA", "RO", "RS", "RU", "RW", "SA",
            "SB", "SC", "SD", "SE", "SG", "SI", "SK", "SL", "SM", "SN",
            "SO", "SR", "SS", "ST", "SV", "SY", "SZ", "TD", "TG", "TH",
            "TJ", "TL", "TM", "TN", "TO", "TR", "TT", "TV", "TZ", "UA",
            "UG", "US", "UY", "UZ", "VA", "VC", "VE", "VN", "VU", "WS",
            "YE", "ZA", "ZM", "ZW"
        )
        val prefix = request.argument().value().uppercase()
        val matches = countryCodes.filter { it.startsWith(prefix) }
        return CompleteResult(CompleteCompletion(matches, matches.size, false))
    }
}
