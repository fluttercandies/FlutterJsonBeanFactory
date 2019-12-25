package com.ruiyu.utils

import java.util.*
import java.util.regex.*

/**
 * Transforms words (from singular to plural, from camelCase to under_score, etc.). I got bored of doing Real Work...
 *
 * @author chuyeow
 * @modified Winter Lau (http://www.oschina.net/)
 */

class Inflector private constructor() {

    init {
        // Woo, you can't touch me.

        initialize()
    }

    private fun initialize() {
        plural("$", "s")
        plural("s$", "s")
        plural("(ax|test)is$", "$1es")
        plural("(octop|vir)us$", "$1i")
        plural("(alias|status)$", "$1es")
        plural("(bu)s$", "$1es")
        plural("(buffal|tomat)o$", "$1oes")
        plural("([ti])um$", "$1a")
        plural("sis$", "ses")
        plural("(?:([^f])fe|([lr])f)$", "$1$2ves")
        plural("(hive)$", "$1s")
        plural("([^aeiouy]|qu)y$", "$1ies")
        plural("([^aeiouy]|qu)ies$", "$1y")
        plural("(x|ch|ss|sh)$", "$1es")
        plural("(matr|vert|ind)ix|ex$", "$1ices")
        plural("([m|l])ouse$", "$1ice")
        plural("(ox)$", "$1es")
        plural("(quiz)$", "$1zes")

        singular("s$", "")
        singular("(n)ews$", "$1ews")
        singular("([ti])a$", "$1um")
        singular("((a)naly|(b)a|(d)iagno|(p)arenthe|(p)rogno|(s)ynop|(t)he)ses$", "$1$2sis")
        singular("(^analy)ses$", "$1sis")
        singular("([^f])ves$", "$1fe")
        singular("(hive)s$", "$1")
        singular("(tive)s$", "$1")
        singular("([lr])ves$", "$1f")
        singular("([^aeiouy]|qu)ies$", "$1y")
        singular("(s)eries$", "$1eries")
        singular("(m)ovies$", "$1ovie")
        singular("(x|ch|ss|sh)es$", "$1")
        singular("([m|l])ice$", "$1ouse")
        singular("(bus)es$", "$1")
        singular("(o)es$", "$1")
        singular("(shoe)s$", "$1")
        singular("(cris|ax|test)es$", "$1is")
        singular("([octop|vir])i$", "$1us")
        singular("(alias|status)es$", "$1")
        singular("^(ox)es", "$1")
        singular("(vert|ind)ices$", "$1ex")
        singular("(matr)ices$", "$1ix")
        singular("(quiz)zes$", "$1")

        irregular("person", "people")
        irregular("man", "men")
        irregular("child", "children")
        irregular("sex", "sexes")
        irregular("move", "moves")

        uncountable(arrayOf("equipment", "information", "rice", "money", "species", "series", "fish", "sheep", "data"))
    }

    fun underscore(camelCasedWord: String): String {

        // Regexes in Java are fucking stupid...
        var underscoredWord = UNDERSCORE_PATTERN_1.matcher(camelCasedWord).replaceAll("$1_$2")
        underscoredWord = UNDERSCORE_PATTERN_2.matcher(underscoredWord).replaceAll("$1_$2")
        underscoredWord = underscoredWord.replace('-', '_').toLowerCase()

        return underscoredWord
    }

    fun pluralize(word: String): String {
        return if (uncountables.contains(word.toLowerCase())) {
            word
        } else replaceWithFirstRule(word, plurals)
    }
    //首字母大写
    fun singularize(word: String): String {
        return if (uncountables.firstOrNull { word.toLowerCase().endsWith(it) } != null) {
            word
        } else replaceWithFirstRule(word, singulars)
    }

    private fun replaceWithFirstRule(word: String, ruleAndReplacements: List<RuleAndReplacement>): String {

        for (rar in ruleAndReplacements) {
            val rule = rar.rule
            val replacement = rar.replacement

            // Return if we find a match.
            val matcher = Pattern.compile(rule, Pattern.CASE_INSENSITIVE).matcher(word)
            if (matcher.find()) {
                return matcher.replaceAll(replacement)
            }
        }
        return word
    }

    fun tableize(className: String): String {
        return pluralize(underscore(className))
    }

    fun tableize(klass: Class<*>): String {
        // Strip away package name - we only want the 'base' class name.
        val className = klass.name.replace(klass.getPackage().name + ".", "")
        return tableize(className)
    }

    companion object {

        // Pfft, can't think of a better name, but this is needed to avoid the price of initializing the pattern on each call.
        private val UNDERSCORE_PATTERN_1 = Pattern.compile("([A-Z]+)([A-Z][a-z])")
        private val UNDERSCORE_PATTERN_2 = Pattern.compile("([a-z\\d])([A-Z])")

        private val plurals = ArrayList<RuleAndReplacement>()
        private val singulars = ArrayList<RuleAndReplacement>()
        private val uncountables = ArrayList<String>()

        private var instance: Inflector? = null // (Pseudo-)Singleton instance.

        fun getInstance(): Inflector {
            if (instance == null) {
                instance = Inflector()
            }
            return instance as Inflector
        }

        fun plural(rule: String, replacement: String) {
            plurals.add(0, RuleAndReplacement(rule, replacement))
        }

        fun singular(rule: String, replacement: String) {
            singulars.add(0, RuleAndReplacement(rule, replacement))
        }

        fun irregular(singular: String, plural: String) {
            plural(singular, plural)
            singular(plural, singular)
        }

        @JvmStatic
        fun uncountable(words: Array<String>) {
            for (word in words) {
                uncountables.add(word)
            }
        }
    }
}


// Ugh, no open structs in Java (not-natively at least).
internal class RuleAndReplacement(var rule: String?, var replacement: String?)




