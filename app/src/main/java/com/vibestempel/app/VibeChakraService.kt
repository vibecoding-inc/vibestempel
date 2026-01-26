package com.vibestempel.app

/**
 * Heimo Schusterzucker's energetischer Vibe-Service.
 * Für die Extra-Portion positive Schwingungen beim Stempeln.
 */
object VibeChakraService {
    private val vibes = listOf(
        "Dein Wurzelchakra ist heute besonders stabil. Zeit für einen Leberkäs-Stempel!",
        "Die Energie fließt! Dein Herzchakra leuchtet wie ein frisch gewarteter Beamer.",
        "Achtung: Merkur ist rückläufig. Erst mal eine Pause machen und die Chakren ausrichten.",
        "Dein Vibe-Level ist heute auf DJ-Niveau. Absolutes Agieren ist angesagt! 🏊‍♂️",
        "Handauflegen hilft: Dein Smartphone hat heute eine besonders reine Aura."
    )

    fun getRandomVibe(): String {
        return vibes.random()
    }
}
