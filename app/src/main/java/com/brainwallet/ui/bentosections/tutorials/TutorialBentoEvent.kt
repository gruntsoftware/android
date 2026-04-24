package com.brainwallet.ui.bentosections.tutorials

sealed class TutorialBentoEvent {
    data object OnLoad : TutorialBentoEvent()
    data object OnTapGeneralTutorial : TutorialBentoEvent()
    data object OnTapSendTutorial : TutorialBentoEvent()
}
