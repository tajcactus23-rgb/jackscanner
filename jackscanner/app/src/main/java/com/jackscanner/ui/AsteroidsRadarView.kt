package com.jackscanner.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.*
import kotlin.random.Random

/**
 * Asteroids-style radar visualization with:
 * - Bottom-mounted emitter
 * - Smooth drifting objects
 * - Detection beam
 * - Particle effects
 * - Idle animation
 * - 60 FPS rendering
 */
class AsteroidsRadarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Paints
    private val radarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }
    
    private val emitterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    
    private val beamPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    
    private val objectPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    
    private val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 24f
    }

    // Colors
    private var radarColor = Color.parseColor("#00E5FF")
    private var gridColor = Color.parseColor("#1A00E5FF")
    private var beamColor = Color.parseColor("#4000E5FF")
    private var objectColor = Color.parseColor("#00E5FF")
    private var emitterColor = Color.parseColor("#00E5FF")
    private var particleColor = Color.parseColor("#6600E5FF")

    // Radar dimensions
    private var centerX = 0f
    private var centerY = 0f
    private var radarRadius = 0f
    private var emitterY = 0f
    
    // Animation state
    private var beamAngle = 0f
    private var isScanning = false
    private var isIdle = true
    private var lastFrameTime = 0L
    
    // Drifting objects (asteroids)
    private val driftingObjects = mutableListOf<DriftingObject>()
    private val maxDriftingObjects = 8
    
    // Detection objects (targets found)
    private val detectionObjects = mutableListOf<DetectionObject>()
    
    // Particles
    private val particles = mutableListOf<Particle>()
    private val maxParticles = 50
    
    // Beam settings
    private val beamLength = 0.85f // As fraction of radar radius
    private val beamWidth = 0.08f // Radians
    
    // Frame rate control
    private val targetFps = 60
    private val frameInterval = 1000L / targetFps
    
    // Detection callback
    var onDetectionListener: OnDetectionListener? = null
    
    interface OnDetectionListener {
        fun onObjectDetected(mac: String, name: String, type: String)
        fun onObjectLost(mac: String)
    }

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
        initDriftingObjects()
    }
    
    private fun initDriftingObjects() {
        driftingObjects.clear()
        for (i in 0 until maxDriftingObjects) {
            driftingObjects.add(createRandomObject())
        }
    }
    
    private fun createRandomObject(): DriftingObject {
        val angle = Random.nextFloat() * 2 * PI.toFloat()
        val distance = Random.nextFloat() * 0.7f + 0.2f // 0.2 to 0.9 of radius
        return DriftingObject(
            x = cos(angle) * distance,
            y = sin(angle) * distance,
            vx = (Random.nextFloat() - 0.5f) * 0.001f,
            vy = (Random.nextFloat() - 0.5f) * 0.001f,
            size = Random.nextFloat() * 4f + 2f,
            alpha = Random.nextFloat() * 0.5f + 0.3f,
            pulsePhase = Random.nextFloat() * 2 * PI.toFloat()
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f
        radarRadius = min(w, h) / 2f * 0.85f
        emitterY = h * 0.92f // Bottom mounted emitter
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val currentTime = System.currentTimeMillis()
        val deltaTime = if (lastFrameTime == 0L) 16L else currentTime - lastFrameTime
        lastFrameTime = currentTime
        
        // Background
        canvas.drawColor(Color.parseColor("#030508"))
        
        // Draw grid
        drawGrid(canvas)
        
        // Draw radar rings
        drawRadarRings(canvas)
        
        // Update and draw drifting objects
        updateDriftingObjects(deltaTime)
        drawDriftingObjects(canvas)
        
        // Draw detection objects
        drawDetectionObjects(canvas)
        
        // Draw particles
        updateParticles(deltaTime)
        drawParticles(canvas)
        
        // Draw detection beam
        if (isScanning) {
            drawBeam(canvas)
        }
        
        // Draw emitter
        drawEmitter(canvas)
        
        // Continue animation
        if (isAttachedToWindow) {
            postInvalidateOnAnimation()
        }
    }
    
    private fun drawGrid(canvas: Canvas) {
        gridPaint.color = gridColor
        
        // Cross lines
        canvas.drawLine(centerX - radarRadius, centerY, centerX + radarRadius, centerY, gridPaint)
        canvas.drawLine(centerX, centerY - radarRadius, centerX, centerY + radarRadius, gridPaint)
        
        // Diagonal lines
        val diagOffset = radarRadius * 0.707f
        canvas.drawLine(centerX - diagOffset, centerY - diagOffset, centerX + diagOffset, centerY + diagOffset, gridPaint)
        canvas.drawLine(centerX - diagOffset, centerY + diagOffset, centerX + diagOffset, centerY - diagOffset, gridPaint)
    }
    
    private fun drawRadarRings(canvas: Canvas) {
        // Outer ring
        radarPaint.color = radarColor
        radarPaint.strokeWidth = 2f
        canvas.drawCircle(centerX, centerY, radarRadius, radarPaint)
        
        // Middle ring
        radarPaint.color = gridColor
        radarPaint.strokeWidth = 1f
        canvas.drawCircle(centerX, centerY, radarRadius * 0.65f, radarPaint)
        
        // Inner ring
        canvas.drawCircle(centerX, centerY, radarRadius * 0.35f, radarPaint)
    }
    
    private fun updateDriftingObjects(deltaTime: Float) {
        for (obj in driftingObjects) {
            // Move object
            obj.x += obj.vx * deltaTime
            obj.y += obj.vy * deltaTime
            
            // Wrap around edges
            if (obj.x < -1f) obj.x = 1f
            if (obj.x > 1f) obj.x = -1f
            if (obj.y < -1f) obj.y = 1f
            if (obj.y > 1f) obj.y = -1f
            
            // Update pulse
            obj.pulsePhase += 0.003f * deltaTime
            if (obj.pulsePhase > 2 * PI.toFloat()) {
                obj.pulsePhase -= 2 * PI.toFloat()
            }
        }
        
        // Occasionally replace an object
        if (Random.nextFloat() < 0.001f * deltaTime && isIdle) {
            val idx = Random.nextInt(driftingObjects.size)
            driftingObjects[idx] = createRandomObject()
        }
    }
    
    private fun drawDriftingObjects(canvas: Canvas) {
        for (obj in driftingObjects) {
            val screenX = centerX + obj.x * radarRadius
            val screenY = centerY + obj.y * radarRadius
            val pulse = (sin(obj.pulsePhase.toDouble()) * 0.3f + 0.7f).toFloat()
            val alpha = (obj.alpha * pulse * 255).toInt()
            
            objectPaint.color = Color.argb(alpha, 0, 229, 255)
            
            // Draw glow
            objectPaint.maskFilter = BlurMaskFilter(obj.size * 2, BlurMaskFilter.Blur.NORMAL)
            canvas.drawCircle(screenX, screenY, obj.size * 2, objectPaint)
            
            // Draw core
            objectPaint.maskFilter = null
            canvas.drawCircle(screenX, screenY, obj.size, objectPaint)
        }
    }
    
    private fun drawDetectionObjects(canvas: Canvas) {
        for (det in detectionObjects) {
            val screenX = centerX + det.x * radarRadius
            val screenY = centerY + det.y * radarRadius
            
            // Pulsing glow
            val pulse = (sin(det.pulsePhase.toDouble()) * 0.3f + 0.7f).toFloat()
            
            // Outer glow
            particlePaint.color = Color.argb((100 * pulse).toInt(), 255, 45, 85)
            particlePaint.maskFilter = BlurMaskFilter(20f * pulse, BlurMaskFilter.Blur.NORMAL)
            canvas.drawCircle(screenX, screenY, 15f * pulse, particlePaint)
            
            // Core
            particlePaint.color = Color.argb(255, 255, 45, 85)
            particlePaint.maskFilter = null
            canvas.drawCircle(screenX, screenY, 8f, particlePaint)
            
            // Update pulse
            det.pulsePhase += 0.05f
            if (det.pulsePhase > 2 * PI.toFloat()) {
                det.pulsePhase -= 2 * PI.toFloat()
            }
        }
    }
    
    private fun updateParticles(deltaTime: Float) {
        // Update existing particles
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.x += p.vx * deltaTime
            p.y += p.vy * deltaTime
            p.life -= deltaTime * 0.002f
            if (p.life <= 0) {
                iterator.remove()
            }
        }
        
        // Spawn new particles when scanning
        if (isScanning && particles.size < maxParticles && Random.nextFloat() < 0.3f) {
            spawnParticle()
        }
    }
    
    private fun spawnParticle() {
        val angle = beamAngle + (Random.nextFloat() - 0.5f) * beamWidth
        val speed = Random.nextFloat() * 2f + 1f
        particles.add(Particle(
            x = cos(angle) * radarRadius * 0.1f,
            y = sin(angle) * radarRadius * 0.1f,
            vx = cos(angle) * speed * 0.001f,
            vy = sin(angle) * speed * 0.001f,
            life = 1f,
            size = Random.nextFloat() * 3f + 1f
        ))
    }
    
    private fun drawParticles(canvas: Canvas) {
        for (p in particles) {
            val screenX = centerX + p.x
            val screenY = centerY + p.y
            val alpha = (p.life * 150).toInt()
            
            particlePaint.color = Color.argb(alpha, 0, 229, 255)
            canvas.drawCircle(screenX, screenY, p.size * p.life, particlePaint)
        }
    }
    
    private fun drawBeam(canvas: Canvas) {
        // Animate beam angle
        beamAngle += 0.02f
        if (beamAngle > 2 * PI.toFloat()) {
            beamAngle -= 2 * PI.toFloat()
        }
        
        // Draw beam as a sweep
        val path = Path()
        val beamEndLength = radarRadius * beamLength
        val halfWidth = beamWidth / 2f
        
        val startAngle = beamAngle - halfWidth
        val endAngle = beamAngle + halfWidth
        
        // Beam gradient
        val beamGradient = RadialGradient(
            centerX, centerY, beamEndLength,
            intArrayOf(
                Color.argb(100, 0, 229, 255),
                Color.argb(50, 0, 229, 255),
                Color.argb(0, 0, 229, 255)
            ),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        
        beamPaint.shader = beamGradient
        beamPaint.style = Paint.Style.FILL
        
        // Draw beam wedge
        path.moveTo(centerX, centerY)
        path.lineTo(
            centerX + cos(startAngle) * beamEndLength,
            centerY + sin(startAngle) * beamEndLength
        )
        path.arcTo(
            RectF(centerX - beamEndLength, centerY - beamEndLength,
                  centerX + beamEndLength, centerY + beamEndLength),
            Math.toDegrees(startAngle.toDouble()).toFloat(),
            Math.toDegrees((endAngle - startAngle).toDouble()).toFloat()
        )
        path.close()
        canvas.drawPath(path, beamPaint)
        
        // Draw beam line
        beamPaint.shader = null
        beamPaint.color = Color.argb(200, 0, 229, 255)
        beamPaint.strokeWidth = 3f
        beamPaint.style = Paint.Style.STROKE
        canvas.drawLine(
            centerX, centerY,
            centerX + cos(beamAngle) * beamEndLength,
            centerY + sin(beamAngle) * beamEndLength,
            beamPaint
        )
    }
    
    private fun drawEmitter(canvas: Canvas) {
        // Emitter base glow
        val emitterGlow = RadialGradient(
            centerX, emitterY, 30f,
            intArrayOf(
                Color.argb(150, 0, 229, 255),
                Color.argb(50, 0, 229, 255),
                Color.argb(0, 0, 229, 255)
            ),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        
        emitterPaint.shader = emitterGlow
        canvas.drawCircle(centerX, emitterY, 30f, emitterPaint)
        
        // Emitter core
        emitterPaint.shader = null
        emitterPaint.color = if (isScanning) {
            Color.argb(255, 0, 229, 255)
        } else {
            Color.argb(150, 0, 229, 255)
        }
        canvas.drawCircle(centerX, emitterY, 8f, emitterPaint)
        
        // Pulsing ring when scanning
        if (isScanning) {
            val pulseRadius = 8f + ((System.currentTimeMillis() % 1000) / 1000f) * 25f
            val pulseAlpha = (1f - (System.currentTimeMillis() % 1000) / 1000f) * 150
            emitterPaint.color = Color.argb(pulseAlpha.toInt(), 0, 229, 255)
            emitterPaint.style = Paint.Style.STROKE
            emitterPaint.strokeWidth = 2f
            canvas.drawCircle(centerX, emitterY, pulseRadius, emitterPaint)
            emitterPaint.style = Paint.Style.FILL
        }
    }
    
    // Public methods for controlling the radar
    
    fun startScanning() {
        isScanning = true
        isIdle = false
        invalidate()
    }
    
    fun stopScanning() {
        isScanning = false
        isIdle = true
        invalidate()
    }
    
    /**
     * Add a detected object to the radar
     */
    fun addDetection(mac: String, name: String, type: String) {
        // Check if already detected
        val existing = detectionObjects.find { it.mac == mac }
        if (existing != null) {
            existing.lastSeen = System.currentTimeMillis()
            existing.x = existing.x * 0.95f + (Random.nextFloat() - 0.5f) * 0.1f
            existing.y = existing.y * 0.95f + (Random.nextFloat() - 0.5f) * 0.1f
            return
        }
        
        // Add new detection at random position
        val angle = Random.nextFloat() * 2 * PI.toFloat()
        val distance = Random.nextFloat() * 0.5f + 0.3f
        detectionObjects.add(DetectionObject(
            mac = mac,
            name = name,
            type = type,
            x = cos(angle) * distance,
            y = sin(angle) * distance,
            lastSeen = System.currentTimeMillis(),
            pulsePhase = 0f
        ))
        
        // Spawn detection particles
        for (i in 0..10) {
            spawnDetectionParticle()
        }
        
        // Notify listener
        onDetectionListener?.onObjectDetected(mac, name, type)
        
        invalidate()
    }
    
    private fun spawnDetectionParticle() {
        val angle = Random.nextFloat() * 2 * PI.toFloat()
        val speed = Random.nextFloat() * 3f + 2f
        particles.add(Particle(
            x = 0f,
            y = 0f,
            vx = cos(angle) * speed * 0.002f,
            vy = sin(angle) * speed * 0.002f,
            life = 1f,
            size = Random.nextFloat() * 4f + 2f
        ))
    }
    
    /**
     * Remove a lost detection
     */
    fun removeDetection(mac: String) {
        detectionObjects.removeIf { it.mac == mac }
        onDetectionListener?.onObjectLost(mac)
        invalidate()
    }
    
    /**
     * Clear all detections
     */
    fun clearDetections() {
        detectionObjects.clear()
        invalidate()
    }
    
    fun getDetectionCount(): Int = detectionObjects.size
    
    fun isCurrentlyScanning(): Boolean = isScanning
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        lastFrameTime = 0L
    }
    
    // Data classes
    data class DriftingObject(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var size: Float,
        var alpha: Float,
        var pulsePhase: Float
    )
    
    data class DetectionObject(
        val mac: String,
        val name: String,
        val type: String,
        var x: Float,
        var y: Float,
        var lastSeen: Long,
        var pulsePhase: Float
    )
    
    data class Particle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var life: Float,
        var size: Float
    )
}
