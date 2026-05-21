package com.gyleedev.selectableappbarandlistexample

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

// 아이템 데이터를 나타내는 데이터 클래스입니다. (데이터 소스용 원본 모델)
// avatar: 사용자 아바타 URL
// login: 사용자 아이디
// name: 사용자 이름
data class ItemModel(
    val avatar: String,
    val login: String,
    val name: String
)

// 프리뷰 및 테스트용 사용자 정보 리스트입니다.
// 별도의 매핑 없이 ItemModel 리스트를 직접 정의하여 사용합니다.
val previewUserInfoItems = listOf(
    ItemModel(
        avatar = "https://avatars.githubusercontent.com/u/32689599?v=4",
        login = "android",
        name = "Android"
    ),
    ItemModel(
        avatar = "https://avatars.githubusercontent.com/u/1342004?v=4",
        login = "google",
        name = "Google"
    ),
    ItemModel(
        avatar = "https://avatars.githubusercontent.com/u/1446536?v=4",
        login = "Kotlin",
        name = "Kotlin"
    ),
    ItemModel(
        avatar = "https://avatars.githubusercontent.com/u/82592?v=4",
        login = "square",
        name = "Square"
    ),
    ItemModel(
        avatar = "https://avatars.githubusercontent.com/u/1267113?v=4",
        login = "kakao",
        name = "kakao"
    ),
    ItemModel(
        avatar = "https://avatars.githubusercontent.com/u/6589568?v=4",
        login = "naver",
        name = "NAVER"
    ),
    ItemModel(
        avatar = "https://avatars.githubusercontent.com/u/7907400?v=4",
        login = "nhn",
        name = "NHN"
    )
)

// 메인 화면의 비즈니스 로직과 데이터를 관리하는 ViewModel 클래스입니다.
@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    // 현재 화면의 모드(NONE, SEARCH, SELECT)를 관리하는 상태 홀더입니다.
    private val _mode = MutableStateFlow(MainMode.NONE)

    // 원본 사용자 데이터 리스트입니다.
    private val _rawItems = MutableStateFlow<List<ItemModel>>(emptyList())

    // 선택된 아이템의 고유 키(login)들을 관리하는 Set입니다.
    private val _selectedLogins = MutableStateFlow<Set<String>>(emptySet())

    // 원본 데이터, 선택된 키 세트, 화면 모드를 결합하여 최종 UI 상태를 생성합니다.
    val uiState: StateFlow<MainUiState> = combine(
        _rawItems,
        _selectedLogins,
        _mode
    ) { items, selectedLogins, mode ->
        if (items.isEmpty()) {
            // 아이템이 없는 경우 로딩 상태를 반환합니다.
            MainUiState.Loading
        } else {
            // UI 레이어에서 요구하는 SelectableUserItem 구조로 변환합니다.
            val selectableItems = items.map { model ->
                SelectableUserItem(
                    login = model.login,
                    name = model.name, // 이름 정보 추가
                    avatar = model.avatar,
                    isSelected = selectedLogins.contains(model.login)
                )
            }
            MainUiState.Success(
                items = selectableItems,
                mode = mode
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState.Loading
    )

    init {
        // ViewModel 생성 시 초기 데이터를 로드합니다.
        loadInitialData()
    }

    // 초기 데이터를 로드하는 내부 함수입니다.
    private fun loadInitialData() {
        _rawItems.value = previewUserInfoItems
    }

    // 화면의 모드를 변경하는 함수입니다.
    fun setMode(mode: MainMode) {
        _mode.value = mode
        if (mode != MainMode.SELECT) {
            clearSelection()
        }
    }

    // 아이템의 선택 상태를 토글하는 함수입니다.
    fun toggleItemSelection(login: String) {
        val currentSelected = _selectedLogins.value.toMutableSet()
        if (currentSelected.contains(login)) {
            currentSelected.remove(login)
        } else {
            currentSelected.add(login)
        }
        _selectedLogins.value = currentSelected

        // 선택 상태에 따른 자동 모드 전환 로직입니다.
        val hasSelection = currentSelected.isNotEmpty()
        if (hasSelection && _mode.value != MainMode.SELECT) {
            _mode.value = MainMode.SELECT
        } else if (!hasSelection && _mode.value == MainMode.SELECT) {
            _mode.value = MainMode.NONE
        }
    }

    // 모든 선택 상태를 초기화하는 내부 함수입니다.
    private fun clearSelection() {
        _selectedLogins.value = emptySet()
    }
}
