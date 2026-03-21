package com.ggardet.mcp.provider

import com.ggardet.mcp.repository.PeopleRepository
import io.quarkiverse.mcp.server.CompleteArg
import io.quarkiverse.mcp.server.CompletePrompt
import io.quarkiverse.mcp.server.CompleteResourceTemplate
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class CompletionProvider {
    @Inject
    lateinit var peopleRepository: PeopleRepository

    @CompleteResourceTemplate(value = "Person Profile")
    fun completeResourceName(@CompleteArg(name ="name") name: String): List<String> {
        val prefix = name.lowercase()
        return peopleRepository.listAll()
            .map { it.name }
            .filter { it.lowercase().startsWith(prefix) }
    }

    @CompletePrompt(value = "people-by-name")
    fun completeName(@CompleteArg(name ="name") name: String): List<String> {
        val prefix = name.lowercase()
        return peopleRepository.listAll()
            .map { it.name }
            .filter { it.lowercase().startsWith(prefix) }
    }

    @CompletePrompt(value = "people-by-country")
    fun completeCountry(@CompleteArg(name ="country") country: String): List<String> {
        val prefix = country.lowercase()
        return peopleRepository.listAll()
            .mapNotNull { it.country }
            .distinct()
            .filter { it.lowercase().startsWith(prefix) }
    }

    @CompletePrompt(value = "weather-lookup")
    fun completeCountryCode(@CompleteArg(name ="countryCode") countryCode: String): List<String> {
        val prefix = countryCode.uppercase()
        return COUNTRY_CODES.filter { it.startsWith(prefix) }.take(MAX_COMPLETIONS)
    }

    companion object {
        private const val MAX_COMPLETIONS = 100
        private val COUNTRY_CODES = listOf(
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
    }
}
