package com.brainwallet.data.model

import com.brainwallet.R

class IntroLanguageResource {
    fun loadResources(): Array<IntroLanguage> {
        return arrayOf<IntroLanguage>(
            IntroLanguage(
                Language.ENGLISH.code,
                Language.ENGLISH.title,
                "The most fun thing to do with your Litecoin!",
                R.raw.english,
                "Are you sure you want to change the language to English?",
                Language.ENGLISH
            ),
            IntroLanguage(
                Language.SPANISH.code,
                Language.SPANISH.title,
                "¡Lo más divertido que puedes hacer con tus Litecoin!",
                R.raw.spanish,
                "¿Estás seguro de que quieres cambiar el idioma a español?",
                Language.SPANISH,
            ),
            IntroLanguage(
                Language.INDONESIAN.code,
                Language.INDONESIAN.title,
                "Hal paling menyenangkan untuk dilakukan dengan Litecoin Anda!",
                R.raw.bahasaindonesia,
                "Yakin ingin mengubah bahasanya ke bahasa Indonesia?",
                Language.INDONESIAN
            ),
            IntroLanguage(
                Language.ARABIC.code,
                Language.ARABIC.title,
                "الشيء الأكثر متعة يمكنك القيام به مع Litecoin الخاص بك!",
                R.raw.arabic,
                "هل أنت متأكد أنك تريد تغيير اللغة إلى الإندونيسية؟",
                Language.ARABIC
            ),
            IntroLanguage(
                Language.UKRAINIAN.code,
                Language.UKRAINIAN.title,
                "Найцікавіша річ, яку можна зробити зі своїм Litecoin!",
                R.raw.ukrainian,
                "Ви впевнені, що хочете змінити мову на українську?",
                Language.UKRAINIAN
            ),
            IntroLanguage(
                Language.RUSSIAN.code,
                Language.RUSSIAN.title,
                "Самое веселое занятие с вашими Litecoin!",
                R.raw.russian,
                "Вы уверены, что хотите сменить язык на русский?",
                Language.RUSSIAN
            ),
            IntroLanguage(
                Language.PORTUGUESE.code,
                Language.PORTUGUESE.title,
                "A coisa mais divertida para fazer com seu Litecoin!",
                R.raw.portugues,
                "Tem certeza de que deseja alterar o idioma para português?",
                Language.PORTUGUESE
            ),
            IntroLanguage(
                Language.KOREAN.code,
                Language.KOREAN.title,
                "라이트코인으로 할 수 있는 가장 재밌는 일!",
                R.raw.korean,
                "언어를 한국어로 변경하시겠습니까?",
                Language.KOREAN
            ),
            IntroLanguage(
                Language.FRENCH.code,
                Language.FRENCH.title,
                "La chose la plus amusante à faire avec votre Litecoin!",
                R.raw.french,
                "Êtes-vous sûr de vouloir changer la langue en français ?",
                Language.FRENCH
            ),
            IntroLanguage(
                Language.CHINESE_TRADITIONAL.code,
                Language.CHINESE_TRADITIONAL.title,
                "用萊特幣做的最有趣的事！",
                R.raw.traditionalchinese,
                "您確定要將語言改為中文嗎？",
                Language.CHINESE_TRADITIONAL
            ),
            IntroLanguage(
                Language.TURKISH.code,
                Language.TURKISH.title,
                "Litecoin'inizle yapabileceğiniz en eğlenceli şey!",
                R.raw.turkish,
                "Dili türkçeye değiştirmek istediğinizden emin misiniz?",
                Language.TURKISH
            ),
            IntroLanguage(
                Language.JAPANESE.code,
                Language.JAPANESE.title,
                "ライトコインでできる最も楽しいこと！",
                R.raw.japanese,
                "言語を日本語に変更してもよろしいですか?",
                Language.JAPANESE
            ),
            IntroLanguage(
                Language.GERMAN.code,
                Language.GERMAN.title,
                "Das macht am meisten Spaß, was Sie mit Ihrem Litecoin machen können!",
                R.raw.deutsch,
                "Sind Sie sicher, dass Sie die Sprache auf Deutsch ändern möchten?",
                Language.GERMAN
            ),
            IntroLanguage(
                    Language.CHINESE_SIMPLIFIED.code,
            Language.CHINESE_SIMPLIFIED.title,
            "用使用萊特幣可以做的最有趣的事情！",
            R.raw.simplifiedchinese,
            "您確定要將語言改為中文嗎？",
            Language.CHINESE_SIMPLIFIED
            ),
            IntroLanguage(
            Language.HINDI.code,
            Language.HINDI.title,
            "अपने लाइटकॉइन के साथ करने के लिए सबसे मज़ेदार चीज़!",
            R.raw.hindi,
            "您確定要將語言改為中文嗎？",
            Language.HINDI
            ),
            IntroLanguage(
                Language.ITALIAN.code,
            Language.ITALIAN.title,
            "La cosa più divertente da fare con i tuoi Litecoin!",
            R.raw.italiano,
            "Sei sicuro di voler cambiare la lingua in italiano?",
            Language.ITALIAN
            ),
            IntroLanguage(
                Language.BLANK.code,
                Language.BLANK.title,
                " ",
                R.raw.english,
                " ",
                Language.BLANK
            ),
        )
    }

    fun findLanguageIndex(language: Language): Int {
        return loadResources().map { intro -> intro.lang }.indexOf(language)
    }
}
