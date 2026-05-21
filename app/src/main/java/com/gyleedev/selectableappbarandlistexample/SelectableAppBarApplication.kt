package com.gyleedev.selectableappbarandlistexample

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// Hilt를 사용하기 위한 커스텀 Application 클래스입니다.
// @HiltAndroidApp 어노테이션은 Hilt의 코드 생성을 트리거하며, 모든 Hilt 컴포넌트의 베이스 클래스가 됩니다.
@HiltAndroidApp
class SelectableAppBarApplication : Application()
