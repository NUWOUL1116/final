package com.tong.watch

import android.os.Bundle
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class MainActivity : AppCompatActivity() {

    // HealthConnectClient 선언
    private lateinit var healthConnectClient: HealthConnectClient
    // ??

    // 권한 요청을 위한 launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            // 권한이 모두 허용된 경우 데이터 읽기
            readHealthData()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // HealthConnectClient 생성
        healthConnectClient = HealthConnectClient.getOrCreate(this)

        // 권한 요청
        requestHealthPermissions()
    }

    // Health Connect 권한 요청
    private fun requestHealthPermissions() {
        val permissions = setOf(
            HealthPermission.createReadPermission(StepsRecord::class) // 걸음 수 읽기 권한 요청
        )

        // 권한 요청 launcher 실행
        requestPermissionLauncher.launch(permissions.toTypedArray())
    }

    // Health Connect API로 걸음 수 데이터 읽기
    private fun readHealthData() {
        // 현재 시간과 24시간 전 시간을 계산
        val startTime = LocalDateTime.now().minusDays(1).toInstant(ZoneOffset.UTC)
        val endTime = LocalDateTime.now().toInstant(ZoneOffset.UTC)

        // 시간 범위를 설정하여 데이터를 가져옴
        val timeRangeFilter = TimeRangeFilter.between(startTime, endTime)

        // Coroutine을 사용하여 비동기로 데이터 처리
        lifecycleScope.launch {
            try {
                val stepsQuery = healthConnectClient.aggregate(
                    StepsRecord::class,
                    timeRangeFilter
                )

                val totalSteps = stepsQuery.total
                runOnUiThread {
                    // UI에서 걸음 수를 TextView에 표시
                    val stepsTextView = findViewById<TextView>(R.id.stepsTextView)
                    stepsTextView.text = "걸음 수: $totalSteps"
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
