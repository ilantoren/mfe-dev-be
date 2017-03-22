var gulp = require('gulp');
var concat = require('gulp-concat');


gulp.task('default',  ['copy'])
gulp.task('copy'  )


gulp.task('watch', function(){
	gulp.watch('scripts/**/*.*', ['default']);
});

var paths = {
    bower: "./bower_components/",
    lib: "./public/"
};
gulp.copy=function(src,dest){
    return gulp.src(src, {base:"."})
        .pipe(gulp.dest(dest));
};
gulp.task("copy", [], function () {
   gulp.src('./bower_components/**/**/*.{js,map,css}' ).pipe( gulp.dest( './public/components'));
});
console.log("built");
