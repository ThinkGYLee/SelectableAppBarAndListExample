package com.gyleedev.selectableappbarandlistexample

// 화면의 동작 모드를 정의하는 Enum 클래스입니다.
enum class MainMode {
    // 기본 모드
    NONE,
    // 검색 모드
    SEARCH,
    // 선택 모드
    SELECT
}

// 선택 가능한 사용자 아이템을 나타내는 데이터 클래스입니다.
// login: 사용자 아이디
// name: 사용자 이름
// avatar: 사용자 아바타 URL
// isSelected: 아이템의 선택 여부
data class SelectableUserItem(
    val login: String,
    val name: String,
    val avatar: String,
    val isSelected: Boolean = false
)

// 메인 화면의 전체 UI 상태를 나타내는 봉인 인터페이스입니다.
sealed interface MainUiState {
    // 데이터를 불러오는 중인 상태입니다.
    data object Loading : MainUiState

    // 데이터를 성공적으로 불러온 상태입니다.
    // items: 사용자 리스트
    // mode: 현재 화면의 모드 (NONE, SEARCH, SELECT)
    data class Success(
        val items: List<SelectableUserItem>,
        val mode: MainMode
    ) : MainUiState
}
