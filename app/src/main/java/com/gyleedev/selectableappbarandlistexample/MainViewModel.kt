package com.gyleedev.selectableappbarandlistexample

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
// 이 데이터는 테스트 환경에서 항상 유지되어야 합니다.
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

    // 현재 검색어를 관리하는 상태 홀더입니다.
    private val _query = MutableStateFlow("")

    // 검색 결과의 상세 상태를 관리하는 상태 홀더입니다.
    private val _searchState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)

    // 검색 중 로딩 상태를 관리합니다.
    private val _isSearchLoading = MutableStateFlow(false)
    val isSearchLoading: StateFlow<Boolean> = _isSearchLoading

    // 모든 상태를 결합하여 최종 UI 상태를 생성합니다.
    val uiState: StateFlow<MainUiState> = combine(
        _rawItems,
        _selectedLogins,
        _mode,
        _query,
        _searchState
    ) { items, selectedLogins, mode, query, searchState ->
        if (items.isEmpty()) {
            MainUiState.Loading
        } else {
            val selectableItems = items.map { model ->
                SelectableUserItem(
                    login = model.login,
                    name = model.name,
                    avatar = model.avatar,
                    isSelected = selectedLogins.contains(model.login)
                )
            }
            MainUiState.Success(
                items = selectableItems,
                mode = mode,
                query = query,
                searchState = searchState
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

    // 데이터를 로드하는 함수입니다.
    private fun loadInitialData() {
        _rawItems.value = previewUserInfoItems
    }

    // 화면의 모드를 변경하는 함수입니다.
    fun setMode(mode: MainMode) {
        _mode.value = mode
        // SELECT 모드에서 벗어날 때 선택된 항목들을 초기화합니다.
        if (mode != MainMode.SELECT) {
            clearSelection()
        }
    }

    // 검색어가 변경될 때 호출되는 함수입니다.
    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    // 실제 검색을 수행하는 함수입니다.
    fun onSearch(query: String) {
        if (query.isBlank()) return

        viewModelScope.launch {
            _isSearchLoading.value = true
            // 실제 네트워크 통신을 흉내내기 위한 딜레이입니다.
            delay(1000)

            // 프리뷰 데이터에서 검색어와 일치하는 사용자를 찾습니다.
            val foundItem = previewUserInfoItems.find { it.login.equals(query, ignoreCase = true) }

            if (foundItem != null) {
                _searchState.value = SearchUiState.Success(
                    login = foundItem.login,
                    name = foundItem.name,
                    bio = "GitHub 유저 ${foundItem.name}입니다.",
                    avatar = foundItem.avatar
                )
            } else {
                _searchState.value = SearchUiState.Idle
            }
            _isSearchLoading.value = false
        }
    }

    // 검색 결과 아이템을 초기화하는 함수입니다.
    fun onSearchItemReset() {
        _searchState.value = SearchUiState.Idle
    }

    // 아이템의 선택 상태를 토글하는 함수입니다.
    fun toggleItemSelection(login: String) {
        // 검색 모드일 때는 선택 기능을 막습니다.
        if (_mode.value == MainMode.SEARCH) return

        val currentSelected = _selectedLogins.value.toMutableSet()
        if (currentSelected.contains(login)) {
            currentSelected.remove(login)
        } else {
            currentSelected.add(login)
        }
        _selectedLogins.value = currentSelected

        // 1개 이상 선택되면 자동으로 SELECT 모드로 진입합니다.
        val hasSelection = currentSelected.isNotEmpty()
        if (hasSelection && _mode.value != MainMode.SELECT) {
            _mode.value = MainMode.SELECT
        }
        // 사용자의 요구사항에 따라, 선택이 모두 해제되더라도 자동으로 NONE 모드로 돌아가지 않습니다.
    }

    // 모든 아이템을 선택하는 함수입니다.
    fun selectAll() {
        val allLogins = _rawItems.value.map { it.login }.toSet()
        _selectedLogins.value = allLogins
        _mode.value = MainMode.SELECT
    }

    // 선택된 아이템을 모두 해제하지만, 선택 모드는 유지하는 함수입니다.
    fun unselectAll() {
        _selectedLogins.value = emptySet()
    }

    // 선택 모드를 완전히 종료하고 선택 상태를 초기화하는 함수입니다.
    fun exitSelectionMode() {
        _selectedLogins.value = emptySet()
        _mode.value = MainMode.NONE
    }

    // 모든 선택 상태를 초기화하는 내부 함수입니다.
    private fun clearSelection() {
        _selectedLogins.value = emptySet()
    }

    // 아이템의 이벤트를 통합 처리하는 핸들러 함수입니다.
    fun handleItemEvent(login: String, event: MainUiEvent) {
        when (_mode.value) {
            // 기본 모드일 때의 처리
            MainMode.NONE -> {
                if (event == MainUiEvent.LONG_CLICK) {
                    // 롱 클릭 시에만 선택 모드로 진입하며 아이템을 선택합니다.
                    toggleItemSelection(login)
                }
                // 일반 클릭(CLICK) 시에는 현재 아무 동작도 하지 않습니다.
            }
            // 선택 모드일 때의 처리
            MainMode.SELECT -> {
                // 선택 모드에서는 클릭과 롱 클릭 모두 선택 상태를 토글합니다.
                toggleItemSelection(login)
            }
            // 검색 모드일 때의 처리
            MainMode.SEARCH -> {
                /* 필요 시 검색 결과 클릭 처리 추가 */
            }
        }
    }
}
