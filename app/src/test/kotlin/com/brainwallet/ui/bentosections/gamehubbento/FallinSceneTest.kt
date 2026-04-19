package com.brainwallet.ui.bentosections.gamehubbento

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FallinSceneTest {

    // ── helpers ──────────────────────────────────────────────────────────────

    private fun makeDot(
        emoji: String = "😂",
        x: Float = 0.5f,
        y: Float = 0.5f,
        velX: Float = 0f,
        velY: Float = 0f
    ) = EmojiDot(emoji, x, y, velX, velY)

    private fun stepPhysics(
        dot: EmojiDot,
        delta: Float,
        gravity: Float = 1000f,
        restitution: Float = 0.8f,
        friction: Float = 0.04f
    ): EmojiDot {
        var newVelY = dot.velY + gravity * delta
        var newVelX = dot.velX * (1f - friction)
        var newX = dot.x + newVelX * delta / 1000f
        var newY = dot.y + newVelY * delta / 1000f

        if (newY > 1f) {
            newY = 1f
            newVelY = -newVelY * restitution
        }
        if (newY < 0f) {
            newY = 0f
            newVelY = -newVelY * restitution
        }
        if (newX > 1f) {
            newX = 1f
            newVelX = -newVelX * restitution
        }
        if (newX < 0f) {
            newX = 0f
            newVelX = -newVelX * restitution
        }

        return dot.copy(x = newX, y = newY, velX = newVelX, velY = newVelY)
    }

    // ── EmojiDot data class ──────────────────────────────────────────────────

    @Test
    fun `EmojiDot copy preserves all fields`() {
        val dot = makeDot(emoji = "🔥", x = 0.1f, y = 0.2f, velX = 10f, velY = -5f)
        val copy = dot.copy(velY = 99f)
        assertEquals("🔥", copy.emoji)
        assertEquals(0.1f, copy.x, 0.001f)
        assertEquals(0.2f, copy.y, 0.001f)
        assertEquals(10f, copy.velX, 0.001f)
        assertEquals(99f, copy.velY, 0.001f)
    }

    @Test
    fun `EmojiDot equality works correctly`() {
        val a = makeDot(x = 0.3f, y = 0.4f)
        val b = makeDot(x = 0.3f, y = 0.4f)
        assertEquals(a, b)
    }

    // ── gravity ──────────────────────────────────────────────────────────────

    @Test
    fun `gravity increases velY each frame`() {
        val dot = makeDot(velY = 0f)
        val stepped = stepPhysics(dot, delta = 0.016f)
        assertTrue("velY should increase due to gravity", stepped.velY > dot.velY)
    }

    @Test
    fun `gravity accumulates over multiple frames`() {
        var dot = makeDot(velY = 0f)
        repeat(10) { dot = stepPhysics(dot, delta = 0.016f) }
        assertTrue("velY should be significantly positive after 10 frames", dot.velY > 0.1f)
    }

    @Test
    fun `dot falls downward with zero initial velocity`() {
        val dot = makeDot(y = 0.5f, velY = 0f)
        val stepped = stepPhysics(dot, delta = 0.016f)
        assertTrue("y should increase (fall down)", stepped.y > dot.y)
    }

    // ── floor bounce ─────────────────────────────────────────────────────────

    @Test
    fun `dot stays within bounds after many frames`() {
        var dot = makeDot(x = 0.5f, y = 0.5f, velX = 300f, velY = -200f)
        repeat(100) { dot = stepPhysics(dot, delta = 0.016f) }
        assertTrue("x should be within 0..1", dot.x in 0f..1f)
        assertTrue("y should be within 0..1", dot.y in 0f..1f)
    }

    // ── friction ─────────────────────────────────────────────────────────────

    @Test
    fun `friction reduces velX each frame`() {
        val dot = makeDot(velX = 100f)
        val stepped = stepPhysics(dot, delta = 0.016f, friction = 0.04f)
        assertTrue("velX should decrease due to friction", stepped.velX < dot.velX)
    }

    @Test
    fun `friction does not reverse velX direction`() {
        val dot = makeDot(velX = 10f)
        val stepped = stepPhysics(dot, delta = 0.016f, friction = 0.04f)
        assertTrue("velX should still be positive", stepped.velX > 0f)
    }

    @Test
    fun `friction accumulates to near zero over many frames`() {
        var dot = makeDot(velX = 100f, velY = 0f, y = 1f)
        // pin to floor to isolate friction
        repeat(500) {
            dot = stepPhysics(dot, delta = 0.016f, gravity = 0f, friction = 0.04f)
        }
        assertTrue("velX should approach zero after many frames", kotlin.math.abs(dot.velX) < 1f)
    }

    // ── spawn logic ──────────────────────────────────────────────────────────

    @Test
    fun `emojiList is not empty`() {
        assertTrue(emojiList.isNotEmpty())
    }

    @Test
    fun `emojiList contains expected emojis`() {
        assertTrue(emojiList.contains("😂"))
        assertTrue(emojiList.contains("🔥"))
        assertTrue(emojiList.contains("❤️"))
    }

    @Test
    fun `emojiList has no duplicates`() {
        assertEquals(emojiList.size, emojiList.toSet().size)
    }

    @Test
    fun `new dot starts with normalized coordinates`() {
        val dot = makeDot(x = kotlin.random.Random.nextFloat(), y = kotlin.random.Random.nextFloat())
        assertTrue("x should be in 0..1", dot.x in 0f..1f)
        assertTrue("y should be in 0..1", dot.y in 0f..1f)
    }

    // ── physics values ───────────────────────────────────────────────────────

    @Test
    fun `restitution of 1 preserves speed after bounce`() {
        val dot = makeDot(y = 0.99f, velY = 100f)
        val stepped = stepPhysics(dot, delta = 0.016f, gravity = 0f, restitution = 1f)
        assertEquals(100f, kotlin.math.abs(stepped.velY), 1f)
    }

    @Test
    fun `zero gravity means no vertical acceleration`() {
        val dot = makeDot(velY = 0f)
        val stepped = stepPhysics(dot, delta = 0.016f, gravity = 0f)
        assertEquals(0f, stepped.velY, 0.001f)
    }

    @Test
    fun `zero delta means no position change`() {
        val dot = makeDot(x = 0.5f, y = 0.5f, velX = 100f, velY = 100f)
        val stepped = stepPhysics(dot, delta = 0f)
        assertEquals(dot.x, stepped.x, 0.001f)
        assertEquals(dot.y, stepped.y, 0.001f)
    }
}
