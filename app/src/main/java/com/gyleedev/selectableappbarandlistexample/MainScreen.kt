package com.gyleedev.selectableappbarandlistexample

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.glide.GlideImage
import com.skydoves.landscapist.placeholder.shimmer.Shimmer
import com.skydoves.landscapist.placeholder.shimmer.ShimmerPlugin

// 선택 가능한 앱바와 리스트 예시 화면을 구성하는 메인 Composable 함수입니다.
@Composable
fun SelectableAppBarAndListExampleScreen(
    // Hilt를 사용하여 ViewModel을 주입받습니다.
    viewModel: MainViewModel = hiltViewModel()
) {
    // ViewModel의 UI 상태를 생명주기에 안전하게 구독합니다.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Scaffold는 앱의 기본적인 시각적 레이아웃 구조를 제공합니다.
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        // 상단에 표시될 커스텀 앱바를 설정합니다. 현재의 UI 상태를 전달합니다.
        topBar = {
            CustomTopBar(uiState)
        }
    ) { innerPadding ->
        // 상태에 따라 다른 화면을 보여줍니다.
        when (val state = uiState) {
            is MainUiState.Loading -> {
                // 로딩 중일 때 표시할 화면입니다.
                LoadingScreen(innerPadding)
            }
            is MainUiState.Success -> {
                // 데이터를 성공적으로 불러왔을 때 리스트를 표시합니다.
                ListContent(
                    paddingValues = innerPadding,
                    items = state.items,
                    onItemClick = { item ->
                        // 아이템 클릭 시 선택 상태를 토글합니다.
                        viewModel.toggleItemSelection(item.login)
                    },
                    onItemLongClick = { item ->
                        // 롱 클릭 시 선택 모드로 진입하며 해당 아이템을 선택합니다.
                        if (state.mode != MainMode.SELECT) {
                            viewModel.toggleItemSelection(item.login)
                        }
                    }
                )
            }
        }
    }
}

// 로딩 화면을 구성하는 Composable 함수입니다.
@Composable
fun LoadingScreen(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        // 중앙에 프로그레스 인디케이터를 표시합니다.
        CircularProgressIndicator()
    }
}

// 앱의 상단 바를 구성하는 Composable 함수입니다.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopBar(uiState: MainUiState) {
    // 현재 화면의 모드에 따라 타이틀을 결정합니다.
    val title = when (uiState) {
        is MainUiState.Success -> {
            when (uiState.mode) {
                MainMode.NONE -> "선택 가능한 리스트 예제"
                MainMode.SEARCH -> "사용자 검색"
                MainMode.SELECT -> {
                    // 선택된 아이템의 개수를 계산하여 타이틀에 표시합니다.
                    val selectedCount = uiState.items.count { it.isSelected }
                    "${selectedCount}개 선택됨"
                }
            }
        }
        else -> "로딩 중..."
    }

    // Material3의 TopAppBar를 사용하여 상단 바를 구현합니다.
    TopAppBar(
        title = {
            Text(text = title)
        }
    )
}

// 리스트 내용을 표시하는 Composable 함수입니다.
@Composable
fun ListContent(
    paddingValues: PaddingValues,
    items: List<SelectableUserItem>,
    onItemClick: (SelectableUserItem) -> Unit,
    onItemLongClick: (SelectableUserItem) -> Unit
) {
    // LazyColumn을 사용하여 효율적인 리스트를 구현합니다.
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        items(
            items = items,
            key = { it.login } // 효율적인 리스트 업데이트를 위해 각 아이템의 고유 키를 지정합니다.
        ) { item ->
            // 각 사용자 정보를 표시하기 위해 커스텀 UserInfoItem을 사용합니다.
            UserInfoItem(
                avatar = item.avatar,
                login = item.login,
                isSelected = item.isSelected,
                onClick = { onItemClick(item) },
                onLongClick = { onItemLongClick(item) }
            )
        }
    }
}

// 사용자 개별 정보를 표시하는 아이템 컴포넌트입니다.
@Composable
fun UserInfoItem(
    avatar: String,
    login: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 선택 여부에 따라 배경색을 변경하여 사용자에게 시각적 피드백을 제공합니다.
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }

    // 가로 방향으로 아바타와 사용자 이름을 배치합니다.
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp, max = 100.dp)
            .background(backgroundColor) // 배경색을 적용합니다.
            // 클릭과 롱 클릭을 모두 지원하는 combinedClickable을 설정합니다.
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // 사용자 아바타 이미지를 표시합니다.
        UserAvatar(avatar = avatar)
        // 사용자 이름을 굵은 텍스트로 표시합니다.
        Text(
            text = login,
            fontWeight = FontWeight.Bold,
        )
    }
}

// 사용자의 아바타 이미지를 불러와서 표시하는 컴포넌트입니다.
@Composable
fun UserAvatar(
    avatar: String,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
) {
    // Glide를 사용하여 이미지를 로드하고 Shimmer 효과를 적용합니다.
    GlideImage(
        imageModel = { avatar },
        modifier = modifier
            .padding(horizontal = 8.dp)
            .size(size)
            .clip(CircleShape),
        // 이미지 로딩 중이나 실패 시의 컴포넌트를 설정합니다.
        component = rememberImageComponent {
            // 쉬머(Shimmer) 효과를 추가하여 로딩 상태를 시각적으로 보여줍니다.
            +ShimmerPlugin(
                Shimmer.Flash(
                    baseColor = Color.White,
                    highlightColor = Color.LightGray,
                ),
            )
        },
    )
}
