package com.example.ringdemo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ToneMapperTest {

    @Test
    fun `normalizeRssiForPitch clamps to zero and one at bounds`() {
        val mapper = ToneMapper()

        assertEquals(0f, mapper.normalizeRssiForPitch(-100f), 1e-6f)
        assertEquals(1f, mapper.normalizeRssiForPitch(-30f), 1e-6f)
        assertEquals(0f, mapper.normalizeRssiForPitch(-120f), 1e-6f)
        assertEquals(1f, mapper.normalizeRssiForPitch(-10f), 1e-6f)
    }

    @Test
    fun `stronger RSSI slides pitch window upward`() {
        val mapper = ToneMapper()
        mapper.setAxisRange('X', 120f, 880f)

        val far = mapper.computePitchWindow(mapper.x, -100f)
        val near = mapper.computePitchWindow(mapper.x, -30f)

        assertTrue("near min should be above far min", near.minHz > far.minHz)
        assertTrue("near max should be above far max", near.maxHz > far.maxHz)
    }

    @Test
    fun `rotation 0 to 125 spans from window min to window max`() {
        val mapper = ToneMapper()
        mapper.setAxisRange('X', 120f, 880f)

        val rssi = -65f
        val window = mapper.computePitchWindow(mapper.x, rssi)

        val low = mapper.mapRotToTonesWithRssi(Vec3(0f, 0f, 0f), rssi)
        val high = mapper.mapRotToTonesWithRssi(Vec3(125f, 0f, 0f), rssi)

        assertEquals(window.minHz, low.fx, 1.5f)
        assertEquals(window.maxHz, high.fx, 1.5f)
    }
}
