var isSpammer = lichess.storage.make('spammer');

var regex = new RegExp([
  'xcamweb.com',
  'chess-bot',
  'coolteenbitch',
  'goo.gl/',
  'letcafa.webcam',
  'tinyurl.com/',
  'wooga.info/',
  'bit.ly/',
  'wbt.link/',
  'eb.by/',
  '001.rs/',
  'shr.name/',
].map(function(url) {
  return url.replace(/\./g, '\\.').replace(/\//g, '\\/');
}).join('|'));

function analyse(txt) {
  return !!txt.match(regex);
}

module.exports = {
  skip: function(txt) {
    return analyse(txt) && isSpammer.get() != '1';
  },
  report: function(txt) {
    if (analyse(txt)) {
      $.post('/jslog/____________?n=spam');
      isSpammer.set(1);
    }
  }
};
