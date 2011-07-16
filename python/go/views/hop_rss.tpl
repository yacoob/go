<rss version="2.0"><channel>
    <title>Go {{title}}</title>
    <link>{{list_url}}</link>
    <description>{{description}}
    </description>
    <pubDate>{{timestamp}}</pubDate>
    <lastBuildDate>{{timestamp}}</lastBuildDate>

% for id in sorted(stack.keys(), reverse=True):
% row = stack[id]
    <item>
        <title>{{row['url']}}</title>
        <link>{{row['pop_url']}}</link>
        <description>{{row['url']}}</description>
        <pubDate>{{row['date']}}</pubDate>
    </item>
% end

</channel></rss>
