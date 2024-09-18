# 文章详解 -> [探索 WebView 加载优化](https://stefanshan.github.io/blog/doc/Android/WebView%E5%8A%A0%E8%BD%BD%E4%BC%98%E5%8C%96.html)
 <table>
    <caption>
    以下数据均为 20次实验平均耗时（去掉最快和最慢的一次）
    </caption>
    <tr>
        <td align="center">测试方式</td>
        <td align="center">耗时</td>
        <td align="center">备注</td>
    </tr>
    <tr>
        <td>串行加载</td>
        <td>2061ms</td>
        <td rowspan="2">串并行加载均为本地资源加载，不与下面的测试对比。</td>
    </tr>
    <tr>
        <td>并行加载</td>
        <td>1285ms</td>
    </tr>
    <tr>
        <td>默认加载</td>
        <td>3030ms</td>
        <td></td>
    </tr>
    <tr>
        <td>预创建 WebView</td>
        <td>2818ms</td>
        <td></td>
    </tr>
    <tr>
        <td>本地缓存</td>
        <td>2657ms</td>
        <td></td>
    </tr>
    <tr>
        <td>预请求</td>
        <td>3490ms</td>
        <td>预请求不符合预期，HTML请求之后响应太慢。可能h5优化后会符合预期。</td>
    </tr>
    <tr>
        <td>预加载</td>
        <td>2356ms</td>
        <td></td>
    </tr>
</table>
