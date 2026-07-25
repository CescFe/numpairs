package org.cescfe.numpairs

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element

class V10LocalizationResourceParityTest {
    @Test
    fun v10_strings_are_complete_and_format_compatible_in_every_supported_locale() {
        val resourcesDirectory = resourcesDirectory()
        val defaultStrings = readStrings(
            File(resourcesDirectory, "values/strings.xml")
        ).filterKeys(::isV10String)
        val supportedLocales = listOf("values-es", "values-ca")

        assertTrue(defaultStrings.isNotEmpty())
        supportedLocales.forEach { localeDirectory ->
            val localizedStrings = readStrings(
                File(resourcesDirectory, "$localeDirectory/strings.xml")
            ).filterKeys(::isV10String)

            assertEquals(defaultStrings.keys, localizedStrings.keys)
            defaultStrings.forEach { (name, defaultValue) ->
                val localizedValue = localizedStrings[name]
                assertNotNull(localizedValue)
                val requiredLocalizedValue = requireNotNull(localizedValue)
                assertTrue("$localeDirectory/$name must not be blank", requiredLocalizedValue.isNotBlank())
                assertEquals(
                    "$localeDirectory/$name must preserve formatting arguments",
                    formatArguments(defaultValue),
                    formatArguments(requiredLocalizedValue)
                )
            }
        }
    }

    private fun readStrings(file: File): Map<String, String> {
        val documentBuilderFactory = DocumentBuilderFactory.newInstance().apply {
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
        }
        val document = documentBuilderFactory.newDocumentBuilder().parse(file)
        return buildMap {
            val nodes = document.getElementsByTagName("string")
            repeat(nodes.length) { index ->
                val element = nodes.item(index) as Element
                put(element.getAttribute("name"), element.textContent)
            }
        }
    }

    private fun resourcesDirectory(): File = listOf(
        File("src/main/res"),
        File("app/src/main/res")
    ).firstOrNull(File::isDirectory)
        ?: error("Android resources directory was not found from ${File(".").absolutePath}.")

    private fun isV10String(name: String): Boolean = name.startsWith("daily_") ||
        name.startsWith("menu_daily_") ||
        name.startsWith("quick_") ||
        name.startsWith("three_pairs_") ||
        name == "menu_play_quick_content_description"

    private fun formatArguments(value: String): List<String> =
        FORMAT_ARGUMENT.findAll(value).map { match -> match.value }.toList()

    private companion object {
        val FORMAT_ARGUMENT = Regex("%(?:\\d+\\$)?[a-zA-Z]")
    }
}
