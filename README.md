# SelectableAppBarAndListExample

이 프로젝트는 Gmail의 아이템 선택 기능(Item Selection) 및 앱바 상태 전환을 학습하고 구현하기 위한 예제 프로젝트입니다.

## 주요 목적
1. **Gmail 스타일의 앱바 전환**: 리스트 아이템 선택 시 검색 바에서 선택된 아이템 개수를 표시하는 '선택 앱바'로 동적 전환
2. **3D Flip 애니메이션**: 선택 시 아바타가 체크 표시 원으로 입체적으로 뒤집히는 효과 구현
3. **상태 관리 실습**: Jetpack Compose를 활용한 다중 선택 상태 및 UI 모드 전환 관리

## 핵심 코드

### 1. 동적 TopAppBar 전환 (Gmail 스타일)
`AnimatedContent`를 사용하여 기본 검색 바와 선택 모드 앱바 사이를 부드럽게 전환합니다.

```kotlin
@Composable
fun MainTopAppBar(uiState: MainUiState, ...) {
    AnimatedContent(
        targetState = (uiState as? MainUiState.Success)?.mode ?: MainMode.NONE,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith
                    fadeOut(animationSpec = tween(300))
        },
        label = "TopBarModeTransition"
    ) { mode ->
        when (mode) {
            MainMode.SELECT -> {
                // 선택 모드 앱바
                SelectionTopBar(...)
            }
            MainMode.NONE, MainMode.SEARCH -> {
                // 기본 검색 바
                EmbeddedSearchBar(...)
            }
        }
    }
}

@Composable
fun SelectionTopBar(items: List<SelectableUserItem>, ...) {
    val selectedCount = items.count { it.isSelected }
    CenterAlignedTopAppBar(
        title = { Text(text = "${selectedCount}개 선택됨") },
        navigationIcon = {
            Checkbox(
                checked = items.all { it.isSelected },
                onCheckedChange = { /* 전체 선택/해제 */ }
            )
        },
        actions = {
            IconButton(onClick = onExitSelection) {
                Icon(imageVector = Icons.Rounded.Close, contentDescription = "Exit")
            }
        }
    )
}
```

### 2. 3D Flip 애니메이션 (아바타 ↔ 체크)
아이템 선택 시 아바타가 체크 표시로 전환되는 애니메이션입니다.
- **참고**: [Dove Letter - 3D Card Flip Animation in Jetpack Compose](https://doveletter.dev/docs/compose-animations/card-flip-3d)

```kotlin
@Composable
fun FlippableUserAvatar(isSelected: Boolean, ...) {
    val rotation by animateFloatAsState(
        targetValue = if (isSelected) 180f else 0f,
        animationSpec = tween(durationMillis = 400)
    )

    Box(
        modifier = Modifier.graphicsLayer {
            rotationY = rotation
            cameraDistance = 12f * density
        }
    ) {
        if (rotation <= 90f) {
            UserAvatar(...) // 앞면
        } else {
            // 뒷면 (체크 아이콘, 반전 방지 처리)
            Box(modifier = Modifier.graphicsLayer { rotationY = 180f }) {
                Icon(imageVector = Icons.Rounded.Check, ...)
            }
        }
    }
}
```

## 예시 영상
- [실행 예시 영상 보기](file:///Users/geumyonglee/Desktop/Screen_recording_20260521_194354.webm)

---
*참고: 위 영상 링크는 현재 로컬 경로로 설정되어 있습니다. GitHub 등에 업로드 시 해당 링크를 온라인 URL로 교체하여 사용하십시오.*
