package com.gyleedev.selectableappbarandlistexample

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.glide.GlideImage
import com.skydoves.landscapist.placeholder.shimmer.Shimmer
import com.skydoves.landscapist.placeholder.shimmer.ShimmerPlugin

// 메인 화면을 구성하는 통합 Composable 함수입니다.
@Composable
fun MainScreen(
    // Hilt를 사용하여 ViewModel을 주입받습니다.
    viewModel: MainViewModel = hiltViewModel()
) {
    // ViewModel의 UI 상태와 검색 로딩 상태를 안전하게 구독합니다.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isSearchLoading by viewModel.isSearchLoading.collectAsStateWithLifecycle()

    // Scaffold는 앱의 기본적인 시각적 레이아웃 구조를 제공합니다.
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        // 분리된 전용 TopAppBar 컴포저블을 호출합니다.
        topBar = {
            MainTopAppBar(
                uiState = uiState,
                isSearchLoading = isSearchLoading,
                onQueryChange = { viewModel.onQueryChange(it) },
                onSearch = { viewModel.onSearch(it) },
                onSearchItemReset = { viewModel.onSearchItemReset() },
                onModeChanged = { isActive ->
                    viewModel.setMode(if (isActive) MainMode.SEARCH else MainMode.NONE)
                },
                onSelectAll = { viewModel.selectAll() },
                onUnselectAll = { viewModel.unselectAll() },
                onExitSelection = { viewModel.exitSelectionMode() }
            )
        }
    ) { innerPadding ->
        // 메인 콘텐츠 영역입니다.
        when (val state = uiState) {
            is MainUiState.Loading -> {
                // 데이터를 불러오는 중일 때의 화면입니다.
                LoadingScreen(innerPadding)
            }

            is MainUiState.Success -> {
                // 데이터를 성공적으로 불러왔을 때의 리스트 화면입니다.
                ListContent(
                    paddingValues = innerPadding,
                    items = state.items,
                    onItemEvent = { login, event ->
                        // 아이템에서 발생하는 이벤트를 ViewModel에 전달합니다.
                        viewModel.handleItemEvent(login, event)
                    }
                )
            }
        }
    }
}

// 모드 전환에 따른 상단 바 구성을 담당하는 통합 컴포저블입니다.
@Composable
fun MainTopAppBar(
    uiState: MainUiState,
    isSearchLoading: Boolean,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onSearchItemReset: () -> Unit,
    onModeChanged: (Boolean) -> Unit,
    onSelectAll: () -> Unit,
    onUnselectAll: () -> Unit,
    onExitSelection: () -> Unit,
) {
    // 현재 모드를 기준으로 애니메이션을 적용합니다.
    AnimatedContent(
        targetState = (uiState as? MainUiState.Success)?.mode ?: MainMode.NONE,
        transitionSpec = {
            // 부드러운 페이드 효과를 적용합니다.
            fadeIn(animationSpec = tween(300)) togetherWith
                    fadeOut(animationSpec = tween(300))
        },
        label = "TopBarModeTransition"
    ) { mode ->
        when (mode) {
            // 선택 모드일 때의 UI입니다.
            MainMode.SELECT -> {
                val state = uiState as MainUiState.Success
                SelectionTopBar(
                    items = state.items,
                    onSelectAll = onSelectAll,
                    onUnselectAll = onUnselectAll,
                    onExitSelection = onExitSelection
                )
            }
            // 기본 또는 검색 모드일 때의 UI입니다.
            MainMode.NONE, MainMode.SEARCH -> {
                val state = uiState as? MainUiState.Success
                EmbeddedSearchBar(
                    onQueryChange = onQueryChange,
                    isSearchActive = mode == MainMode.SEARCH,
                    query = state?.query ?: "",
                    searchState = state?.searchState ?: SearchUiState.Idle,
                    onActiveChanged = onModeChanged,
                    onSearch = onSearch,
                    onSearchItemReset = onSearchItemReset,
                    moveToDetail = { /* TODO */ },
                    loading = isSearchLoading
                )
            }
        }
    }
}

// 데이터를 로딩 중일 때 표시할 화면입니다.
@Composable
fun LoadingScreen(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

// 상단에 임베드된 검색 바 컴포넌트입니다.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmbeddedSearchBar(
    onQueryChange: (String) -> Unit,
    isSearchActive: Boolean,
    query: String,
    searchState: SearchUiState,
    onActiveChanged: (Boolean) -> Unit,
    onSearch: (String) -> Unit,
    onSearchItemReset: () -> Unit,
    moveToDetail: (String) -> Unit,
    loading: Boolean,
    modifier: Modifier = Modifier,
) {
    // 검색 활성화 여부에 따라 좌우 패딩을 애니메이션으로 조절합니다.
    val animatePadding by animateDpAsState(
        targetValue = if (isSearchActive) 0.dp else 24.dp,
        label = "animatePadding",
    )

    SearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = onSearch,
                expanded = isSearchActive,
                onExpandedChange = onActiveChanged,
                placeholder = { Text(text = "사용자 아이디를 검색하세요") },
                leadingIcon = {
                    if (isSearchActive) {
                        IconButton(onClick = { onActiveChanged(false) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "검색 취소",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "검색",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                trailingIcon = {
                    if (isSearchActive && query.isNotEmpty()) {
                        IconButton(onClick = {
                            onQueryChange("")
                            onSearchItemReset()
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "검색어 삭제",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                },
            )
        },
        expanded = isSearchActive,
        onExpandedChange = onActiveChanged,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = animatePadding),
    ) {
        if (searchState is SearchUiState.Success) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
                    .navigationBarsPadding()
                    .imePadding(),
            ) {
                SearchResultItem(
                    onClick = moveToDetail,
                    modifier = Modifier.align(Alignment.TopCenter),
                    login = searchState.login,
                    name = searchState.name,
                    bio = searchState.bio,
                    avatar = searchState.avatar,
                )

                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                Button(
                    onClick = { onSearch(query) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                ) {
                    Text("검색 수행")
                }
            }
        }
    }
}

// 검색 결과 아이템의 상세 정보를 표시하는 컴포넌트입니다.
@Composable
fun SearchResultItem(
    onClick: (String) -> Unit,
    login: String,
    name: String,
    bio: String,
    avatar: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        UserAvatar(avatar = avatar, size = 120.dp)
        Text(
            text = name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "@$login",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = bio,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

// 선택 모드일 때 표시되는 중앙 정렬 상단 앱바입니다.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionTopBar(
    items: List<SelectableUserItem>,
    onSelectAll: () -> Unit,
    onUnselectAll: () -> Unit,
    onExitSelection: () -> Unit
) {
    val selectedCount = items.count { it.isSelected }
    val isAllSelected = items.isNotEmpty() && items.all { it.isSelected }

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "${selectedCount}개 선택됨",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            Checkbox(
                checked = isAllSelected,
                onCheckedChange = { checked ->
                    if (checked) onSelectAll() else onUnselectAll()
                },
                modifier = Modifier.padding(start = 8.dp)
            )
        },
        actions = {
            IconButton(onClick = onExitSelection) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "선택 종료",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    )
}

// 메인 사용자 리스트를 표시하는 Composable 함수입니다.
@Composable
fun ListContent(
    paddingValues: PaddingValues,
    items: List<SelectableUserItem>,
    onItemEvent: (String, MainUiEvent) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        items(
            items = items,
            key = { it.login }
        ) { item ->
            // 개별 사용자 정보 아이템을 표시하며 이벤트를 전달합니다.
            UserInfoItem(
                avatar = item.avatar,
                login = item.login,
                isSelected = item.isSelected,
                onEvent = { event -> onItemEvent(item.login, event) }
            )
        }
    }
}

// 개별 사용자 정보를 가로로 배치하여 표시하는 컴포넌트입니다.
@Composable
fun UserInfoItem(
    avatar: String,
    login: String,
    isSelected: Boolean,
    onEvent: (MainUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 선택 여부에 따라 배경색을 은은하게 변경합니다. (애니메이션과 조화)
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    } else {
        Color.Transparent
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp, max = 100.dp)
            .background(backgroundColor)
            // 클릭과 롱 클릭 이벤트를 모두 처리합니다.
            .combinedClickable(
                onClick = { onEvent(MainUiEvent.CLICK) },
                onLongClick = { onEvent(MainUiEvent.LONG_CLICK) },
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // 3D 플립 애니메이션이 적용된 아바타 컴포넌트입니다.
        FlippableUserAvatar(
            avatar = avatar,
            isSelected = isSelected
        )
        Text(
            text = login,
            fontWeight = FontWeight.Bold,
        )
    }
}

// 선택 상태에 따라 3D 플립 애니메이션을 수행하는 아바타 컴포넌트입니다.
@Composable
fun FlippableUserAvatar(
    avatar: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp
) {
    // 선택 여부에 따라 0도에서 180도까지 회전하는 애니메이션 상태를 생성합니다.
    val rotation by animateFloatAsState(
        targetValue = if (isSelected) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "avatarFlip"
    )

    // 3D 효과를 구현하기 위한 레이어 설정입니다.
    Box(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .size(size)
            .graphicsLayer {
                // Y축을 기준으로 회전시킵니다.
                rotationY = rotation
                // 원근감을 주어 입체적인 느낌을 강화합니다.
                cameraDistance = 12f * density
            },
        contentAlignment = Alignment.Center
    ) {
        // 회전 각도가 90도 이하일 때는 앞면(아바타)을 보여줍니다.
        if (rotation <= 90f) {
            UserAvatar(
                avatar = avatar,
                modifier = Modifier.fillMaxSize(),
                size = size
            )
        } else {
            // 회전 각도가 90도를 넘으면 뒷면(체크 표시 원)을 보여줍니다.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // 이미지가 반전되어 보이지 않도록 다시 180도 회전시킵니다.
                        rotationY = 180f
                    }
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "선택됨",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(size * 0.6f)
                )
            }
        }
    }
}

// 사용자의 아바타 이미지를 표시하는 기본 컴포넌l트입니다.
@Composable
fun UserAvatar(
    avatar: String,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
) {
    // Glide를 사용하여 이미지를 로드하고 쉬머 효과를 적용합니다.
    GlideImage(
        imageModel = { avatar },
        modifier = modifier
            .size(size)
            .clip(CircleShape),
        component = rememberImageComponent {
            // 로딩 중에 보여줄 쉬머 플러그인을 추가합니다.
            +ShimmerPlugin(
                Shimmer.Flash(
                    baseColor = Color.White,
                    highlightColor = Color.LightGray,
                ),
            )
        },
    )
}
