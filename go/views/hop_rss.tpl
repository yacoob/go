<rss version="2.0"><channel>
    <title>Go {{title}}</title>
    <link>{{list_url}}</link>
    <description>{{description}}
    </description>
    <pubDate>{{timestamp}}</pubDate>
    <lastBuildDate>{{timestamp}}</lastBuildDate>

% from email.utils import formatdate
% for row in stack:
    <item>
        <title>{{row['url']}}</title>
        <link>{{row['pop_url']}}</link>
        <description>{{row['url']}}</description>
        <pubDate>{{row['date']}}</pubDate>
    </item>
% end

</channel></rss>
