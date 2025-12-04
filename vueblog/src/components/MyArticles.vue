<template>
  <div class="my-articles">
    <el-row>
      <el-col :span="24">
        <!-- 筛选和操作栏 -->
        <div class="toolbar">
          <el-radio-group v-model="filterState" size="small" @change="filterArticles">
            <el-radio-button label="all">全部</el-radio-button>
            <el-radio-button label="published">已发表</el-radio-button>
            <el-radio-button label="draft">草稿</el-radio-button>
            <el-radio-button label="deleted">回收站</el-radio-button>
          </el-radio-group>
          
          <div class="search-box">
            <el-input
              placeholder="搜索文章标题..."
              v-model="searchKeyword"
              size="small"
              clearable
              @keyup.enter.native="filterArticles">
              <el-button slot="append" icon="el-icon-search" @click="filterArticles"></el-button>
            </el-input>
          </div>
          
          <el-button type="primary" size="small" icon="el-icon-plus" @click="goToPostArticle">
            写文章
          </el-button>
        </div>

        <!-- 文章列表 -->
        <div class="article-list" v-loading="loading">
          <el-card class="article-item" v-for="article in filteredArticles" :key="article.id">
            <div class="article-header">
              <h3 class="article-title" @click="viewArticle(article)">{{ article.title }}</h3>
              <div class="article-actions">
                <el-button 
                  v-if="article.state === 0 || article.state === 1" 
                  type="text" 
                  size="mini" 
                  @click="editArticle(article)">
                  编辑
                </el-button>
                <el-button 
                  v-if="article.state === 2" 
                  type="text" 
                  size="mini" 
                  @click="restoreArticle(article)">
                  还原
                </el-button>
                <el-button 
                  type="text" 
                  size="mini" 
                  style="color: #F56C6C"
                  @click="deleteArticle(article)">
                  {{ article.state === 2 ? '彻底删除' : '删除' }}
                </el-button>
              </div>
            </div>
            
            <div class="article-summary">
              {{ article.summary || article.htmlContent ? stripHtml(article.htmlContent).substring(0, 150) + '...' : '' }}
            </div>
            
            <div class="article-meta">
              <span class="meta-item">
                <i class="el-icon-view"></i>
                {{ article.pageView || 0 }} 浏览
              </span>
              <span class="meta-item">
                <i class="el-icon-edit"></i>
                {{ article.editTime | formatDateTime }}
              </span>
              <span class="meta-item">
                <el-tag 
                  :type="getStateType(article.state)" 
                  size="mini">
                  {{ getStateText(article.state) }}
                </el-tag>
              </span>
              <span class="meta-item tags">
                <el-tag 
                  v-for="tag in article.tags" 
                  :key="tag.id" 
                  size="mini" 
                  type="info"
                  style="margin-right: 5px;">
                  {{ tag.tagName }}
                </el-tag>
              </span>
            </div>
          </el-card>
          
          <!-- 空状态 -->
          <div v-if="filteredArticles.length === 0 && !loading" class="empty-state">
            <i class="el-icon-document"></i>
            <p>暂无文章</p>
            <el-button type="primary" size="small" @click="goToPostArticle">写第一篇文章</el-button>
          </div>
        </div>

        <!-- 分页 -->
        <div class="pagination" v-if="filteredArticles.length > 0">
          <el-pagination
            background
            layout="prev, pager, next"
            :current-page="currentPage"
            :page-size="pageSize"
            :total="total"
            @current-change="handlePageChange">
          </el-pagination>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
  import {getRequest} from '../utils/api'
  import {deleteRequest} from '../utils/api'
  import {putRequest} from '../utils/api'
  export default {
    name: 'MyArticles',
    data() {
      return {
        loading: false,
        articles: [],
        filteredArticles: [],
        filterState: 'all',
        searchKeyword: '',
        currentPage: 1,
        pageSize: 10,
        total: 0
      }
    },
    mounted() {
      this.loadArticles();
    },
    methods: {
      loadArticles() {
        this.loading = true;
        const _this = this;
        
        // 获取当前用户ID
        getRequest("/currentUserId").then(resp => {
          if (resp.status == 200) {
            const userId = resp.data;
            // 获取用户的文章列表
            getRequest(`/article/user/${userId}?page=${_this.currentPage}&size=${_this.pageSize}`).then(articleResp => {
              if (articleResp.status == 200) {
                _this.articles = articleResp.data.list || [];
                _this.total = articleResp.data.total || 0;
                _this.filterArticles();
              }
            }).catch(() => {
              // 如果没有专门的接口，尝试获取所有文章然后过滤
              getRequest("/article/all").then(allResp => {
                if (allResp.status == 200) {
                  _this.articles = allResp.data || [];
                  _this.total = _this.articles.length;
                  _this.filterArticles();
                }
              });
            });
          }
        }).finally(() => {
          this.loading = false;
        });
      },
      
      filterArticles() {
        let filtered = [...this.articles];
        
        // 按状态过滤
        if (this.filterState !== 'all') {
          const stateMap = {
            'published': 1,
            'draft': 0,
            'deleted': 2
          };
          filtered = filtered.filter(article => article.state === stateMap[this.filterState]);
        }
        
        // 按关键词搜索
        if (this.searchKeyword) {
          filtered = filtered.filter(article => 
            article.title.toLowerCase().includes(this.searchKeyword.toLowerCase())
          );
        }
        
        this.filteredArticles = filtered;
      },
      
      viewArticle(article) {
        this.$router.push({
          path: '/blogDetail',
          query: { aid: article.id }
        });
      },
      
      editArticle(article) {
        this.$router.push({
          path: '/editBlog',
          query: { aid: article.id }
        });
      },
      
      goToPostArticle() {
        this.$router.push('/postArticle');
      },
      
      deleteArticle(article) {
        const action = article.state === 2 ? '彻底删除' : '删除';
        this.$confirm(`确定要${action}这篇文章吗？`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          const url = article.state === 2 ? `/article/delete/${article.id}` : `/article/${article.id}`;
          const method = article.state === 2 ? deleteRequest : putRequest;
          
          method(url).then(resp => {
            if (resp.status == 200 && resp.data.status === 'success') {
              this.$message.success(`${action}成功`);
              this.loadArticles();
            } else {
              this.$message.error(`${action}失败`);
            }
          }).catch(() => {
            this.$message.error(`${action}失败`);
          });
        });
      },
      
      restoreArticle(article) {
        this.$confirm('确定要还原这篇文章吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info'
        }).then(() => {
          putRequest(`/article/restore/${article.id}`).then(resp => {
            if (resp.status == 200 && resp.data.status === 'success') {
              this.$message.success('还原成功');
              this.loadArticles();
            } else {
              this.$message.error('还原失败');
            }
          }).catch(() => {
            this.$message.error('还原失败');
          });
        });
      },
      
      handlePageChange(page) {
        this.currentPage = page;
        this.loadArticles();
      },
      
      getStateType(state) {
        const types = {
          0: 'info',    // 草稿
          1: 'success', // 已发表
          2: 'danger'   // 已删除
        };
        return types[state] || 'info';
      },
      
      getStateText(state) {
        const texts = {
          0: '草稿',
          1: '已发表',
          2: '回收站'
        };
        return texts[state] || '未知';
      },
      
      stripHtml(html) {
        if (!html) return '';
        return html.replace(/<[^>]*>/g, '');
      }
    }
  }
</script>

<style scoped>
  .my-articles {
    padding: 20px;
  }
  
  .toolbar {
    display: flex;
    align-items: center;
    gap: 20px;
    margin-bottom: 20px;
    padding: 15px;
    background: #f5f5f5;
    border-radius: 4px;
  }
  
  .search-box {
    flex: 1;
    max-width: 300px;
  }
  
  .article-list {
    min-height: 400px;
  }
  
  .article-item {
    margin-bottom: 15px;
  }
  
  .article-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 10px;
  }
  
  .article-title {
    margin: 0;
    font-size: 16px;
    color: #303133;
    cursor: pointer;
    transition: color 0.3s;
  }
  
  .article-title:hover {
    color: #409EFF;
  }
  
  .article-actions {
    display: flex;
    gap: 10px;
  }
  
  .article-summary {
    color: #606266;
    font-size: 14px;
    line-height: 1.6;
    margin-bottom: 10px;
  }
  
  .article-meta {
    display: flex;
    align-items: center;
    gap: 15px;
    font-size: 12px;
    color: #909399;
  }
  
  .meta-item {
    display: flex;
    align-items: center;
    gap: 3px;
  }
  
  .tags {
    margin-left: auto;
  }
  
  .empty-state {
    text-align: center;
    padding: 60px 0;
    color: #909399;
  }
  
  .empty-state i {
    font-size: 48px;
    margin-bottom: 15px;
    display: block;
  }
  
  .pagination {
    text-align: center;
    margin-top: 20px;
  }
</style>