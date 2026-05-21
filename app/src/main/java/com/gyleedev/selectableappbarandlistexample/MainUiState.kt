package com.gyleedev.selectableappbarandlistexample

// 리스트 아이템에서 발생하는 UI 이벤트를 정의하는 Enum 클래스입니다.
enum class MainUiEvent {
    // 일반 클릭 이벤트
    CLICK,
    // 롱 클릭 이벤트
    LONG_CLICK
}

// 화면의 동작 모드를 정의하는 Enum 클래스입니다.
enum class MainMode {
    // 기본 모드
    NONE,

    // 검색 모드
    SEARCH,

    // 선택 모드
    SELECT
}

// 검색 결과의 상태를 정의하는 봉인 인터페이스입니다.
sealed interface SearchUiState {
    // 검색 전 또는 결과가 없는 상태입니다.
    data object Idle : SearchUiState

    // 검색 중인 상태입니다.
    data object Loading : SearchUiState

    // 검색 성공 시 표시할 데이터 상태입니다.
    data class Success(
        val login: String,
        val name: String,
        val bio: String,
        val avatar: String
    ) : SearchUiState
}

// 선택 가능한 사용자 아이템을 나타내는 데이터 클래스입니다.
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
    // items: 전체 사용자 리스트
    // mode: 현재 동작 모드 (NONE, SEARCH, SELECT)
    // query: 현재 검색어
    // searchState: 검색 결과의 상세 상태
    data class Success(
        val items: List<SelectableUserItem>,
        val mode: MainMode,
        val query: String,
        val searchState: SearchUiState = SearchUiState.Idle
    ) : MainUiState
}
